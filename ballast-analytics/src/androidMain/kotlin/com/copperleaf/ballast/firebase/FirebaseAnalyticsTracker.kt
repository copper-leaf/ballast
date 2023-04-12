package com.copperleaf.ballast.firebase

import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.logEvent

class FirebaseAnalyticsTracker(
    private val analytics: FirebaseAnalytics,
) : AnalyticsTracker {
    override fun trackAnalyticsEvent(eventId: String, eventParameters: Map<String, String>) {
        analytics.logEvent(eventId) {
            eventParameters.entries.forEach { (key, value) ->
                param(key, value)
            }
        }
    }
}
