package com.copperleaf.ballast.analytics

public interface AnalyticsTracker {

    public fun trackAnalyticsEvent(eventId: String, eventParameters: Map<String, String>)

}
