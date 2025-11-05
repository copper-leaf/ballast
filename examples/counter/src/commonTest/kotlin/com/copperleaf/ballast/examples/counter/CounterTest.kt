package com.copperleaf.ballast.examples.counter

import com.copperleaf.ballast.eventHandler
import com.copperleaf.ballast.test.viewModelTest
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class CounterTest {
    @Test
    fun doTest() = runTest {
        viewModelTest(
            inputHandler = CounterInputHandler(),
            eventHandler = eventHandler { },
        ) {
            defaultInitialState { CounterContract.State() }

            scenario("Test Increment by 1") {
                running {
                    +CounterContract.Inputs.Increment(1)
                }
                resultsIn {
                    assertEquals(1, latestState.count)
                    assertEquals(
                        listOf(
                            CounterContract.State(0),
                            CounterContract.State(1),
                        ), states
                    )
                    assertEquals(0, events.size)
                }
            }

            scenario("Test Increment by 10") {
                running {
                    +CounterContract.Inputs.Increment(10)
                }
                resultsIn {
                    assertEquals(10, latestState.count)
                    assertEquals(
                        listOf(
                            CounterContract.State(0),
                            CounterContract.State(10),
                        ), states
                    )
                    assertEquals(
                        listOf(
                            CounterContract.Events.OnTenReached,
                        ), events
                    )
                }
            }

            scenario("Test Decrement by 1") {
                running {
                    +CounterContract.Inputs.Decrement(1)
                }
                resultsIn {
                    assertEquals(-1, latestState.count)
                    assertEquals(
                        listOf(
                            CounterContract.State(0),
                            CounterContract.State(-1),
                        ), states
                    )
                    assertEquals(0, events.size)
                }
            }

            scenario("Test Decrement by 10") {
                running {
                    +CounterContract.Inputs.Decrement(10)
                }
                resultsIn {
                    assertEquals(-10, latestState.count)
                    assertEquals(
                        listOf(
                            CounterContract.State(0),
                            CounterContract.State(-10),
                        ), states
                    )
                    assertEquals(0, events.size)
                }
            }

            scenario("Test Reset") {
                running {
                    +CounterContract.Inputs.Increment(10)
                    +CounterContract.Inputs.Reset
                }
                resultsIn {
                    assertEquals(0, latestState.count)
                    assertEquals(
                        listOf(
                            CounterContract.State(0),
                            CounterContract.State(10),
                            CounterContract.State(0),
                        ), states
                    )
                    assertEquals(
                        listOf(
                            CounterContract.Events.OnTenReached,
                        ), events
                    )
                }
            }

            scenario("Test Event only sent on Increment") {
                given { CounterContract.State(11) }
                running {
                    +CounterContract.Inputs.Decrement(1)
                }
                resultsIn {
                    assertEquals(10, latestState.count)
                    assertEquals(
                        listOf(
                            CounterContract.State(11),
                            CounterContract.State(10),
                        ), states
                    )
                    assertEquals(0, events.size)
                }
            }
        }
    }
}
