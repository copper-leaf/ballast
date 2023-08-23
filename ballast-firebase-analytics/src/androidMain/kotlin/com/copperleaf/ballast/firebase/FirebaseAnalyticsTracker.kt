package com.copperleaf.ballast.firebase

import com.copperleaf.ballast.analytics.AnalyticsTracker
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.logEvent

public class FirebaseAnalyticsTracker(
    private val analytics: FirebaseAnalytics,
) : AnalyticsTracker {
    override fun trackAnalyticsEvent(eventId: String, eventParameters: Map<String, String>) {
        analytics.logEvent(eventId) {
            for((key, value) in eventParameters.entries) {
                param(key, value)
            }
        }
    }

    override fun toString(): String {
        return "FirebaseAnalyticsTracker"
    }
}
