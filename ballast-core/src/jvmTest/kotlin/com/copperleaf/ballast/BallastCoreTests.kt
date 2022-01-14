package com.copperleaf.ballast

import com.copperleaf.ballast.core.FifoInputStrategy
import com.copperleaf.ballast.core.LifoInputStrategy
import com.copperleaf.ballast.core.ParallelInputStrategy
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.yield
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.time.ExperimentalTime

@ExperimentalTime
@ExperimentalCoroutinesApi
class BallastCoreTests {

    data class State(
        val acceptedFilteredValue: Boolean = false,
        val stringValue: String = "",
        val intValue: Int = 0,
    )

    sealed class Inputs {
        object FilteredValue : Inputs()
        object ThrowErrorDuringHandling : Inputs()
        data class UpdateStringValue(val stringValue: String) : Inputs()
        object Increment : Inputs()
        object Decrement : Inputs()
        object IncrementWithRollback : Inputs()
        object MultipleStateUpdates : Inputs()
        object EventEmitted : Inputs()
        object SideEffectStartedNoInputOverride : Inputs()
        object SideEffectStartedWithInputOverride : Inputs()
        object MultipleSideEffects : Inputs()
        object SideEffectsNotAtEnd : Inputs()
    }

    sealed class Events {
        object Notification : Events()
    }

    class TestInputHandler : InputHandler<Inputs, Events, State> {
        override suspend fun InputHandlerScope<Inputs, Events, State>.handleInput(
            input: Inputs
        ) = when (input) {
            is Inputs.FilteredValue -> {
                updateState { it.copy(acceptedFilteredValue = true) }
            }
            is Inputs.ThrowErrorDuringHandling -> {
                delay(1000)
                error("error thrown during handling")
            }
            is Inputs.UpdateStringValue -> {
                delay(1000)
                updateState { it.copy(stringValue = input.stringValue) }
            }
            is Inputs.Increment -> {
                delay(1000)
                updateState { it.copy(intValue = it.intValue + 1) }
            }
            is Inputs.Decrement -> {
                delay(1000)
                updateState { it.copy(intValue = it.intValue - 1) }
            }
            is Inputs.IncrementWithRollback -> {
                updateState { it.copy(intValue = it.intValue + 1) }
                delay(1000)
            }
            is Inputs.MultipleStateUpdates -> {
                updateState { it.copy(intValue = it.intValue + 1) }
                yield()
                updateState { it.copy(intValue = it.intValue + 1) }
                delay(1000)
            }
            is Inputs.EventEmitted -> {
                postEvent(Events.Notification)
            }
            is Inputs.SideEffectStartedNoInputOverride -> {
                sideEffect {
                    delay(1500)

                    flowOf("one", "two", "three")
                        .onEach { delay(1500) }
                        .map { Inputs.UpdateStringValue(it) }
                        .collect { postInput(it) }

                    delay(1500)
                }
            }
            is Inputs.SideEffectStartedWithInputOverride -> {
                sideEffect {
                    delay(1500)

                    flowOf("one", "two", "three")
                        .map { Inputs.UpdateStringValue(it) }
                        .collect { postInput(it) }

                    delay(1500)
                }
            }
            is Inputs.MultipleSideEffects -> {
                sideEffect("one") {
                    delay(1500)
                    postInput(Inputs.Increment)
                    delay(1500)
                    postInput(Inputs.Increment)
                    delay(1500)
                }
                sideEffect("two") {
                    delay(1500)
                    postInput(Inputs.Increment)
                    delay(1500)
                    postInput(Inputs.Increment)
                    delay(1500)
                }
            }
            is Inputs.SideEffectsNotAtEnd -> {
                sideEffect {
                    delay(1500)
                    postInput(Inputs.Increment)
                    delay(1500)
                    postInput(Inputs.Increment)
                    delay(1500)
                }
                getCurrentState()
                Unit
            }
        }
    }

    class TestEventHandler : EventHandler<Inputs, Events, State> {
        override suspend fun EventHandlerScope<Inputs, Events, State>.handleEvent(
            event: Events
        ) = when (event) {
            is Events.Notification -> {}
        }
    }

    class TestInputFilter : InputFilter<Inputs, Events, State> {
        override fun filterInput(state: State, input: Inputs): InputFilter.Result = when (input) {
            is Inputs.FilteredValue -> InputFilter.Result.Reject
            else -> InputFilter.Result.Accept
        }
    }

    @Test
    fun doTest() = runBlockingViewModelTest(
        inputHandler = TestInputHandler(),
        eventHandler = TestEventHandler(),
        filter = TestInputFilter(),
    ) {
        defaultInitialState { State() }

        scenario("update string value only") {
            running {
                +Inputs.UpdateStringValue("one")
            }
            resultsIn {
                assertEquals("one", latestState.stringValue)
                assertEquals(0, latestState.intValue)
            }
        }

        scenario("increment int value only") {
            running {
                +Inputs.Increment
            }
            resultsIn {
                assertEquals(1, latestState.intValue)
            }
        }

        scenario("decrement int value only") {
            running {
                +Inputs.Decrement
            }
            resultsIn {
                assertEquals(-1, latestState.intValue)
            }
        }

        scenario("multiple value updates") {
            given { State(intValue = -1) }
            running {
                +Inputs.Increment
                +Inputs.Increment
                +Inputs.Decrement
                +Inputs.UpdateStringValue("two")
                +Inputs.Increment
            }
            resultsIn {
                assertEquals("two", latestState.stringValue)
                assertEquals(1, latestState.intValue)
            }
        }

        scenario("filtered inputs dropped") {
            running {
                +Inputs.FilteredValue
                +Inputs.Increment
            }
            resultsIn {
                assertEquals(1, latestState.intValue)
                assertFalse(latestState.acceptedFilteredValue)
                assertEquals(
                    listOf(
                        Inputs.FilteredValue,
                    ),
                    rejectedInputs
                )
            }
        }

        scenario("cancelled inputs dropped") {
            running {
                -Inputs.Increment
                -Inputs.Decrement
                -Inputs.UpdateStringValue("one")
                +Inputs.Increment
                +Inputs.Increment
            }
            resultsIn {
                assertEquals(2, latestState.intValue)
                assertEquals(
                    listOf(
                        Inputs.Increment,
                        Inputs.Decrement,
                        Inputs.UpdateStringValue("one"),
                    ),
                    cancelledInputs
                )
                assertEquals(
                    listOf(
                        Inputs.Increment,
                        Inputs.Increment,
                    ),
                    successfulInputs
                )
            }
        }

        scenario("cancelled input state rolled back") {
            running {
                +Inputs.Increment // updates to 1
                +Inputs.Increment // updates to 2
                -Inputs.IncrementWithRollback // should update to 3 if allowed to run to completion...
                +Inputs.Decrement // but it gets cancelled instead. Its update is rolled back, and state updates to 1
            }
            resultsIn {
                assertEquals(1, latestState.intValue)
                assertEquals(listOf(Inputs.IncrementWithRollback), cancelledInputs)
                assertEquals(
                    listOf(
                        Inputs.Increment,
                        Inputs.Increment,
                        Inputs.Decrement,
                    ),
                    successfulInputs
                )
                assertEquals(
                    listOf(
                        State(intValue = 0), // Initial state
                        State(intValue = 1), // Increment applied successfully
                        State(intValue = 2), // Increment applied successfully
                        State(intValue = 3), // the state is applied eagerly...
                        State(intValue = 2), // but because the input was cancelled, it gets rolled back
                        State(intValue = 1), // and then Decrement is applied successfully
                    ),
                    states
                )
            }
        }

        scenario("multiple state updates in 1 input") {
            running {
                +Inputs.MultipleStateUpdates // updates to 2
            }
            resultsIn {
                assertEquals(2, latestState.intValue)
                assertEquals(
                    listOf(
                        State(intValue = 0), // Initial state
                        State(intValue = 1), // MultipleStateUpdates first application
                        State(intValue = 2), // MultipleStateUpdates second application
                    ),
                    states
                )
            }
        }

        scenario("multiple state updates in 1 input rolled back") {
            running {
                -Inputs.MultipleStateUpdates // should update to 2 if allowed to run to completion...
                +Inputs.Decrement // but it gets cancelled instead. Its update is rolled back, and state updates to -1
            }
            resultsIn {
                assertEquals(-1, latestState.intValue)
                assertEquals(listOf(Inputs.MultipleStateUpdates), cancelledInputs)
                assertEquals(listOf(Inputs.Decrement), successfulInputs)
                assertEquals(
                    listOf(
                        State(intValue = 0), // Initial state
                        State(intValue = 1), // MultipleStateUpdates second application
                        State(intValue = 2), // MultipleStateUpdates second application
                        State(intValue = 0), // but because the input was cancelled, it gets rolled back
                        State(intValue = -1), // and then Decrement is applied successfully
                    ),
                    states
                )
            }
        }

        scenario("error thrown") {
            running {
                +Inputs.ThrowErrorDuringHandling
            }
            resultsIn {
                assertEquals(1, inputHandlerErrors.size)
                assertEquals("error thrown during handling", inputHandlerErrors.first().second.message)
            }
        }

        scenario("event posted") {
            running {
                +Inputs.EventEmitted
            }
            resultsIn {
                assertEquals(listOf(Events.Notification), events)
            }
        }

        scenario("sideEffectStarted with inputs that run slowly and do not override each other") {
            running {
                +Inputs.SideEffectStartedNoInputOverride
                +Inputs.Increment
            }
            resultsIn {
                assertEquals(
                    listOf(
                        Inputs.SideEffectStartedNoInputOverride,
                        Inputs.Increment,
                        Inputs.UpdateStringValue("one"),
                        Inputs.UpdateStringValue("two"),
                        Inputs.UpdateStringValue("three"),
                    ),
                    successfulInputs
                )

                assertEquals(State(intValue = 1, stringValue = "three"), latestState)
            }
        }

        scenario("sideEffectStarted with inputs that run quickly and override each other") {
            running {
                +Inputs.SideEffectStartedWithInputOverride
                +Inputs.Increment
            }
            resultsIn {
                assertEquals(
                    listOf(
                        Inputs.SideEffectStartedWithInputOverride,
                        Inputs.Increment,
                        Inputs.UpdateStringValue("three"),
                    ),
                    successfulInputs
                )

                assertEquals(State(intValue = 1, stringValue = "three"), latestState)
            }
        }

        scenario("LIFO strategy with quick multiple updates") {
            inputStrategy { LifoInputStrategy() }
            running {
                -Inputs.MultipleStateUpdates
                -Inputs.MultipleStateUpdates
                +Inputs.MultipleStateUpdates
            }
            resultsIn {
                assertEquals(
                    listOf(
                        Inputs.MultipleStateUpdates,
                    ),
                    successfulInputs,
                )
                assertEquals(
                    listOf(
                        Inputs.MultipleStateUpdates,
                        Inputs.MultipleStateUpdates,
                    ),
                    cancelledInputs,
                )

                assertEquals(State(intValue = 2), latestState)
            }
        }

        scenario("FIFO strategy with quick multiple updates") {
            inputStrategy { FifoInputStrategy() }
            running {
                -Inputs.MultipleStateUpdates
                -Inputs.MultipleStateUpdates
                -Inputs.MultipleStateUpdates
            }
            resultsIn {
                assertEquals(
                    listOf(
                        Inputs.MultipleStateUpdates,
                        Inputs.MultipleStateUpdates,
                        Inputs.MultipleStateUpdates,
                    ),
                    successfulInputs
                )

                assertEquals(State(intValue = 6), latestState)
            }
        }

        scenario("Parallel strategy with quick multiple updates") {
            inputStrategy { ParallelInputStrategy() }
            running {
                -Inputs.MultipleStateUpdates
                -Inputs.MultipleStateUpdates
                +Inputs.MultipleStateUpdates
            }
            resultsIn {
                assertEquals(
                    listOf(
                        Inputs.MultipleStateUpdates,
                        Inputs.MultipleStateUpdates,
                        Inputs.MultipleStateUpdates,
                    ),
                    inputHandlerErrors.map { it.first }
                )

                assertEquals(State(intValue = 6), latestState)
            }
        }
        scenario("Multiple side effects can be started by 1 input") {
            inputStrategy { ParallelInputStrategy() }
            running {
                +Inputs.MultipleSideEffects
            }
            resultsIn {
                assertEquals(
                    listOf(
                        Inputs.MultipleSideEffects,
                        Inputs.Increment,
                        Inputs.Increment,
                        Inputs.Increment,
                        Inputs.Increment,
                    ),
                    successfulInputs,
                )

                assertEquals(State(intValue = 4), latestState)
            }
        }
        scenario("Side effects called before other inputHandler methods throws an error") {
            inputStrategy { ParallelInputStrategy() }
            running {
                +Inputs.SideEffectsNotAtEnd
            }
            resultsIn {
                assertEquals(
                    listOf(
                        Inputs.SideEffectsNotAtEnd,
                    ),
                    inputHandlerErrors.map { it.first }
                )

                assertEquals(State(intValue = 0), latestState)
            }
        }
    }
}
