package com.copperleaf.ballast

import com.copperleaf.ballast.core.FifoInputStrategy
import com.copperleaf.ballast.core.LifoInputStrategy
import com.copperleaf.ballast.core.ParallelInputStrategy
import com.copperleaf.ballast.impl.TestContract
import com.copperleaf.ballast.impl.TestEventHandler
import com.copperleaf.ballast.impl.TestInputFilter
import com.copperleaf.ballast.impl.TestInputHandler
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.time.ExperimentalTime

@ExperimentalTime
@ExperimentalCoroutinesApi
class BallastCoreTests {

    @Test
    fun doTest() = runBlockingViewModelTest(
        inputHandler = TestInputHandler(),
        eventHandler = TestEventHandler(),
        filter = TestInputFilter(),
    ) {
        defaultInitialState { TestContract.State() }

        scenario("update string value only") {
            running {
                +TestContract.Inputs.UpdateStringValue("one")
            }
            resultsIn {
                assertEquals("one", latestState.stringValue)
                assertEquals(0, latestState.intValue)
            }
        }

        scenario("increment int value only") {
            running {
                +TestContract.Inputs.Increment
            }
            resultsIn {
                assertEquals(1, latestState.intValue)
            }
        }

        scenario("decrement int value only") {
            running {
                +TestContract.Inputs.Decrement
            }
            resultsIn {
                assertEquals(-1, latestState.intValue)
            }
        }

        scenario("multiple value updates") {
            given { TestContract.State(intValue = -1) }
            running {
                +TestContract.Inputs.Increment
                +TestContract.Inputs.Increment
                +TestContract.Inputs.Decrement
                +TestContract.Inputs.UpdateStringValue("two")
                +TestContract.Inputs.Increment
            }
            resultsIn {
                assertEquals("two", latestState.stringValue)
                assertEquals(1, latestState.intValue)
            }
        }

        scenario("filtered inputs dropped") {
            running {
                +TestContract.Inputs.FilteredValue
                +TestContract.Inputs.Increment
            }
            resultsIn {
                assertEquals(1, latestState.intValue)
                assertFalse(latestState.acceptedFilteredValue)
                assertEquals(
                    listOf(
                        TestContract.Inputs.FilteredValue,
                    ),
                    rejectedInputs
                )
            }
        }

        scenario("cancelled inputs dropped") {
            running {
                -TestContract.Inputs.Increment
                -TestContract.Inputs.Decrement
                -TestContract.Inputs.UpdateStringValue("one")
                +TestContract.Inputs.Increment
                +TestContract.Inputs.Increment
            }
            resultsIn {
                assertEquals(2, latestState.intValue)
                assertEquals(
                    listOf(
                        TestContract.Inputs.Increment,
                        TestContract.Inputs.Decrement,
                        TestContract.Inputs.UpdateStringValue("one"),
                    ),
                    cancelledInputs
                )
                assertEquals(
                    listOf(
                        TestContract.Inputs.Increment,
                        TestContract.Inputs.Increment,
                    ),
                    successfulInputs
                )
            }
        }

        scenario("cancelled input state rolled back") {
            running {
                +TestContract.Inputs.Increment // updates to 1
                +TestContract.Inputs.Increment // updates to 2
                -TestContract.Inputs.IncrementWithRollback // should update to 3 if allowed to run to completion...
                +TestContract.Inputs.Decrement // but it gets cancelled instead. Its update is rolled back, and state updates to 1
            }
            resultsIn {
                assertEquals(1, latestState.intValue)
                assertEquals(listOf(TestContract.Inputs.IncrementWithRollback), cancelledInputs)
                assertEquals(
                    listOf(
                        TestContract.Inputs.Increment,
                        TestContract.Inputs.Increment,
                        TestContract.Inputs.Decrement,
                    ),
                    successfulInputs
                )
                assertEquals(
                    listOf(
                        TestContract.State(intValue = 0), // Initial state
                        TestContract.State(intValue = 1), // Increment applied successfully
                        TestContract.State(intValue = 2), // Increment applied successfully
                        TestContract.State(intValue = 3), // the state is applied eagerly...
                        TestContract.State(intValue = 2), // but because the input was cancelled, it gets rolled back
                        TestContract.State(intValue = 1), // and then Decrement is applied successfully
                    ),
                    states
                )
            }
        }

        scenario("multiple state updates in 1 input") {
            running {
                +TestContract.Inputs.MultipleStateUpdates // updates to 2
            }
            resultsIn {
                assertEquals(2, latestState.intValue)
                assertEquals(
                    listOf(
                        TestContract.State(intValue = 0), // Initial state
                        TestContract.State(intValue = 1), // MultipleStateUpdates first application
                        TestContract.State(intValue = 2), // MultipleStateUpdates second application
                    ),
                    states
                )
            }
        }

        scenario("multiple state updates in 1 input rolled back") {
            running {
                -TestContract.Inputs.MultipleStateUpdates // should update to 2 if allowed to run to completion...
                +TestContract.Inputs.Decrement // but it gets cancelled instead. Its update is rolled back, and state updates to -1
            }
            resultsIn {
                assertEquals(-1, latestState.intValue)
                assertEquals(listOf(TestContract.Inputs.MultipleStateUpdates), cancelledInputs)
                assertEquals(listOf(TestContract.Inputs.Decrement), successfulInputs)
                assertEquals(
                    listOf(
                        TestContract.State(intValue = 0), // Initial state
                        TestContract.State(intValue = 1), // MultipleStateUpdates second application
                        TestContract.State(intValue = 2), // MultipleStateUpdates second application
                        TestContract.State(intValue = 0), // but because the input was cancelled, it gets rolled back
                        TestContract.State(intValue = -1), // and then Decrement is applied successfully
                    ),
                    states
                )
            }
        }

        scenario("error thrown") {
            running {
                +TestContract.Inputs.ThrowErrorDuringHandling
            }
            resultsIn {
                assertEquals(1, inputHandlerErrors.size)
                assertEquals("error thrown during handling", inputHandlerErrors.first().second.message)
            }
        }

        scenario("event posted") {
            running {
                +TestContract.Inputs.EventEmitted
            }
            resultsIn {
                assertEquals(listOf(TestContract.Events.Notification), events)
            }
        }

        scenario("sideJobStarted with inputs that run slowly and do not override each other") {
            running {
                +TestContract.Inputs.SideJobStartedNoInputOverride
                +TestContract.Inputs.Increment
            }
            resultsIn {
                assertEquals(
                    listOf(
                        TestContract.Inputs.SideJobStartedNoInputOverride,
                        TestContract.Inputs.Increment,
                        TestContract.Inputs.UpdateStringValue("one"),
                        TestContract.Inputs.UpdateStringValue("two"),
                        TestContract.Inputs.UpdateStringValue("three"),
                    ),
                    successfulInputs
                )

                assertEquals(TestContract.State(intValue = 1, stringValue = "three"), latestState)
            }
        }

        scenario("sideJobStarted with inputs that run quickly and override each other") {
            running {
                +TestContract.Inputs.SideJobStartedWithInputOverride
                +TestContract.Inputs.Increment
            }
            resultsIn {
                assertEquals(
                    listOf(
                        TestContract.Inputs.SideJobStartedWithInputOverride,
                        TestContract.Inputs.Increment,
                        TestContract.Inputs.UpdateStringValue("three"),
                    ),
                    successfulInputs
                )

                assertEquals(TestContract.State(intValue = 1, stringValue = "three"), latestState)
            }
        }

        scenario("LIFO strategy with quick multiple updates") {
            inputStrategy { LifoInputStrategy() }
            running {
                -TestContract.Inputs.MultipleStateUpdates
                -TestContract.Inputs.MultipleStateUpdates
                +TestContract.Inputs.MultipleStateUpdates
            }
            resultsIn {
                assertEquals(
                    listOf(
                        TestContract.Inputs.MultipleStateUpdates,
                    ),
                    successfulInputs,
                )
                assertEquals(
                    listOf(
                        TestContract.Inputs.MultipleStateUpdates,
                        TestContract.Inputs.MultipleStateUpdates,
                    ),
                    cancelledInputs,
                )

                assertEquals(TestContract.State(intValue = 2), latestState)
            }
        }

        scenario("FIFO strategy with quick multiple updates") {
            inputStrategy { FifoInputStrategy() }
            running {
                -TestContract.Inputs.MultipleStateUpdates
                -TestContract.Inputs.MultipleStateUpdates
                +TestContract.Inputs.MultipleStateUpdates
            }
            resultsIn {
                assertEquals(
                    listOf(
                        TestContract.Inputs.MultipleStateUpdates,
                        TestContract.Inputs.MultipleStateUpdates,
                        TestContract.Inputs.MultipleStateUpdates,
                    ),
                    successfulInputs
                )

                assertEquals(TestContract.State(intValue = 6), latestState)
            }
        }

        scenario("Parallel strategy with quick multiple updates") {
            inputStrategy { ParallelInputStrategy() }
            running {
                -TestContract.Inputs.MultipleStateUpdates
                -TestContract.Inputs.MultipleStateUpdates
                +TestContract.Inputs.MultipleStateUpdates
            }
            resultsIn {
                assertEquals(
                    listOf(
                        TestContract.Inputs.MultipleStateUpdates,
                        TestContract.Inputs.MultipleStateUpdates,
                        TestContract.Inputs.MultipleStateUpdates,
                    ),
                    inputHandlerErrors.map { it.first }
                )

                assertEquals(TestContract.State(intValue = 3), latestState)
            }
        }

        scenario("Multiple side-jobs can be started by 1 input") {
            inputStrategy { ParallelInputStrategy() }
            running {
                +TestContract.Inputs.MultipleSideJobs
            }
            resultsIn {
                assertEquals(
                    listOf(
                        "one" to SideJobScope.RestartState.Initial,
                        "two" to SideJobScope.RestartState.Initial,
                    ),
                    sideJobs,
                )

                assertEquals(
                    listOf(
                        TestContract.Inputs.MultipleSideJobs,
                        TestContract.Inputs.Increment,
                        TestContract.Inputs.Increment,
                        TestContract.Inputs.Increment,
                        TestContract.Inputs.Increment,
                    ),
                    successfulInputs,
                )

                assertEquals(TestContract.State(intValue = 4), latestState)
            }
        }

        scenario("Side-jobs can be restarted, with previous ones cancelled") {
            inputStrategy { ParallelInputStrategy() }
            running {
                +TestContract.Inputs.MultipleSideJobs
                +TestContract.Inputs.MultipleSideJobs
            }
            resultsIn {
                assertEquals(
                    listOf(
                        "one" to SideJobScope.RestartState.Initial,
                        "two" to SideJobScope.RestartState.Initial,
                        "one" to SideJobScope.RestartState.Restarted,
                        "two" to SideJobScope.RestartState.Restarted,
                    ),
                    sideJobs,
                )

                assertEquals(
                    listOf(
                        TestContract.Inputs.MultipleSideJobs,
                        TestContract.Inputs.MultipleSideJobs,
                        TestContract.Inputs.Increment,
                        TestContract.Inputs.Increment,
                        TestContract.Inputs.Increment,
                        TestContract.Inputs.Increment,
                    ),
                    successfulInputs,
                )

                assertEquals(TestContract.State(intValue = 4), latestState)
            }
        }

        scenario("Side-jobs called before other inputHandler methods throws an error") {
            inputStrategy { ParallelInputStrategy() }
            running {
                +TestContract.Inputs.SideJobsNotAtEnd
            }
            resultsIn {
                assertEquals(
                    listOf(
                        TestContract.Inputs.SideJobsNotAtEnd,
                    ),
                    inputHandlerErrors.map { it.first }
                )
            }
        }

        scenario("Skipped test") {
            skip()
            running {
                +TestContract.Inputs.Increment
            }
            resultsIn {}
        }

        isolatedScenario(TestContract.Inputs.Increment, "increment from 0") {
            resultsIn {
                assertEquals(
                    TestContract.State(intValue = 1),
                    latestState
                )
            }
        }

        isolatedScenario(TestContract.Inputs.Increment, "increment from 1") {
            given { TestContract.State(intValue = 1) }
            resultsIn {
                assertEquals(
                    TestContract.State(intValue = 2),
                    latestState
                )
            }
        }
    }
}
