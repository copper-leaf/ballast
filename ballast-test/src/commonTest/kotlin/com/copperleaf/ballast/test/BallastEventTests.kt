package com.copperleaf.ballast.test

import com.copperleaf.ballast.EventHandler
import com.copperleaf.ballast.EventHandlerScope
import com.copperleaf.ballast.InputHandler
import com.copperleaf.ballast.InputHandlerScope
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlin.time.ExperimentalTime

private object EventTestContract {
    data class State(val something: Int = 0)

    sealed interface Input {
        data class EventReceived(val something: Int) : Input
    }

    sealed interface Event {
        data class SomethingCool(val something: Int) : Event
    }

    class Reducer : InputHandler<Input, Event, State> {
        override suspend fun InputHandlerScope<Input, Event, State>.handleInput(input: Input) {
            when (input) {
                is Input.EventReceived -> updateState { current -> current.copy(something = input.something) }
            }
        }
    }

    class EventProcessor : EventHandler<Input, Event, State> {
        override suspend fun EventHandlerScope<Input, Event, State>.handleEvent(event: Event) {
            when (event) {
                is Event.SomethingCool -> postInput(Input.EventReceived(event.something))
            }
        }
    }
}

private const val TEST_CONSTANT = 1337

@OptIn(ExperimentalTime::class, ExperimentalCoroutinesApi::class)
class BallastEventTests : StringSpec({
    coroutineTestScope = true

    "confirm event processing" {
        viewModelTest(
            inputHandler = EventTestContract.Reducer(),
            eventHandler = EventTestContract.EventProcessor()
        ) {
            defaultInitialState { EventTestContract.State() }

            scenario("sending a single event") {
                running {
                    postEvent(EventTestContract.Event.SomethingCool(TEST_CONSTANT))
                }

                resultsIn {
                    states.size shouldBe 2
                    states.first().something shouldBe 0
                    latestState.something shouldBe TEST_CONSTANT
                }
            }
        }
    }
})
