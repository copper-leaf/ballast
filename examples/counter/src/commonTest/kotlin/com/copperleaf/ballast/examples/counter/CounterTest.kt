package com.copperleaf.ballast.examples.counter

import com.copperleaf.ballast.eventHandler
import com.copperleaf.ballast.test.viewModelTest
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe

class CounterTest : StringSpec({
    "doTest" {
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
                    latestState.count shouldBe 1
                    states shouldBe listOf(
                        CounterContract.State(0),
                        CounterContract.State(1),
                    )
                    events.shouldBeEmpty()
                }
            }

            scenario("Test Increment by 10") {
                running {
                    +CounterContract.Inputs.Increment(10)
                }
                resultsIn {
                    latestState.count shouldBe 10
                    states shouldBe listOf(
                        CounterContract.State(0),
                        CounterContract.State(10),
                    )
                    events shouldBe listOf(
                        CounterContract.Events.OnTenReached,
                    )
                }
            }

            scenario("Test Decrement by 1") {
                running {
                    +CounterContract.Inputs.Decrement(1)
                }
                resultsIn {
                    latestState.count shouldBe -1
                    states shouldBe listOf(
                        CounterContract.State(0),
                        CounterContract.State(-1),
                    )
                    events.shouldBeEmpty()
                }
            }

            scenario("Test Decrement by 10") {
                running {
                    +CounterContract.Inputs.Decrement(10)
                }
                resultsIn {
                    latestState.count shouldBe -10
                    states shouldBe listOf(
                        CounterContract.State(0),
                        CounterContract.State(-10),
                    )
                    events.shouldBeEmpty()
                }
            }

            scenario("Test Reset") {
                running {
                    +CounterContract.Inputs.Increment(10)
                    +CounterContract.Inputs.Reset
                }
                resultsIn {
                    latestState.count shouldBe 0
                    states shouldBe listOf(
                        CounterContract.State(0),
                        CounterContract.State(10),
                        CounterContract.State(0),
                    )
                    events shouldBe listOf(
                        CounterContract.Events.OnTenReached,
                    )
                }
            }

            scenario("Test Event only sent on Increment") {
                given { CounterContract.State(11) }
                running {
                    +CounterContract.Inputs.Decrement(1)
                }
                resultsIn {
                    latestState.count shouldBe 10
                    states shouldBe listOf(
                        CounterContract.State(11),
                        CounterContract.State(10),
                    )
                    events.shouldBeEmpty()
                }
            }
        }
    }
})
