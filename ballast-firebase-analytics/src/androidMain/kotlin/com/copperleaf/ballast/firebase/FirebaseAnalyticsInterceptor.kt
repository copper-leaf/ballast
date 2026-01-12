package com.copperleaf.ballast.firebase

import com.copperleaf.ballast.analytics.AnalyticsInterceptor
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase

public fun <Inputs : Any, Events : Any, State : Any> FirebaseAnalyticsInterceptor(
    analytics: FirebaseAnalytics = Firebase.analytics,
    shouldTrackInput: (Inputs) -> Boolean = { it.isAnnotatedWith<FirebaseAnalyticsTrackInput>() },
): AnalyticsInterceptor<Inputs, Events, State> {
    return AnalyticsInterceptor(
        tracker = FirebaseAnalyticsTracker(analytics),
        shouldTrackInput = shouldTrackInput,
    )
}

public inline fun <reified Ann : Annotation> Any.isAnnotatedWith(): Boolean {
    return this::class.java.isAnnotationPresent(Ann::class.java)
}
