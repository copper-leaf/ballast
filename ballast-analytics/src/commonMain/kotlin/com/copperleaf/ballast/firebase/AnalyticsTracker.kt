package com.copperleaf.ballast.firebase

public interface AnalyticsTracker {

    public fun trackAnalyticsEvent(eventId: String, eventParameters: Map<String, String>)

}
