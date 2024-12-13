package com.copperleaf.ballast.analytics

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
}

private class TestAnalyticsTracker : AnalyticsTracker {
    override fun trackAnalyticsEvent(eventId: String, eventParameters: Map<String, String>) {
        TODO("Not yet implemented")
    }

    override fun toString(): String {
        return "TestAnalyticsTracker"
    }
}
