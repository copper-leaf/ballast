package com.copperleaf.ballast.analytics

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class BallastAnalyticsTests : StringSpec({
    "check toString values" {
        AnalyticsInterceptor<Any, Any, Any>(
            tracker = TestAnalyticsTracker(),
            shouldTrackInput = { true },
        ).toString() shouldBe "AnalyticsInterceptor(tracker=TestAnalyticsTracker)"
    }
})

private class TestAnalyticsTracker : AnalyticsTracker {
    override fun trackAnalyticsEvent(eventId: String, eventParameters: Map<String, String>) {
        TODO("Not yet implemented")
    }

    override fun toString(): String {
        return "TestAnalyticsTracker"
    }
}
