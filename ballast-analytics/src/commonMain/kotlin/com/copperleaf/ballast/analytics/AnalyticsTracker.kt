package com.copperleaf.ballast.analytics

public interface AnalyticsTracker {

    /**
     * Record an event with an analytics SDK.
     */
    public fun trackAnalyticsEvent(eventId: String, eventParameters: Map<String, String>)

}
