package com.copperleaf.ballast.analytics

import com.copperleaf.ballast.analytics.vm.TestContract
import com.copperleaf.ballast.analytics.vm.TestInputHandler
import com.copperleaf.ballast.core.FifoInputStrategy
import com.copperleaf.ballast.eventHandler
import com.copperleaf.ballast.test.viewModelTest
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class BallastAnalyticsTests {
    @Test
    fun checkToStringValues() = runTest {
        assertEquals(
            "AnalyticsInterceptor(tracker=TestAnalyticsTracker)", AnalyticsInterceptor<Any, Any, Any>(
                tracker = TestAnalyticsTracker(),
                shouldTrackInput = { true },
            ).toString()
        )
    }

    @Test
    fun testAnalyticsTrackerToString() = runTest {
        viewModelTest(
            inputHandler = TestInputHandler(),
            eventHandler = eventHandler { },
        ) {
            val trackedInputs = mutableListOf<Pair<String, Map<String, String>>>()

            defaultInputStrategy { FifoInputStrategy.typed() }
            defaultInitialState { TestContract.State() }
            addInterceptor {
                AnalyticsInterceptor(
                    tracker = AnalyticsTracker { eventId, eventParameters ->
                        trackedInputs += eventId to eventParameters
                    },
                    shouldTrackInput = { input ->
                        when (input) {
                            is TestContract.Inputs.TrackThis -> true
                            is TestContract.Inputs.DontTrackThis -> false
                        }
                    }
                )
            }

            scenario("AnalyticsInterceptorTest") {
                running {
                    +TestContract.Inputs.TrackThis
                    +TestContract.Inputs.DontTrackThis
                }
                resultsIn {
                    assertEquals(
                        actual = trackedInputs,
                        expected = listOf(
                            "action" to mapOf(
                                "ViewModelName" to "AnalyticsInterceptorTest",
                                "InputType" to "AnalyticsInterceptorTest.TrackThis",
                                "InputValue" to "AnalyticsInterceptorTest.TrackThis",
                            )
                        )
                    )
                }
            }
        }
    }
}

private class TestAnalyticsTracker : AnalyticsTracker {
    override fun trackAnalyticsEvent(eventId: String, eventParameters: Map<String, String>) {
        TODO("Not yet implemented")
    }

    override fun toString(): String {
        return "TestAnalyticsTracker"
    }
}
