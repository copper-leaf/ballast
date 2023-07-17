package com.copperleaf.ballast.firebase

import com.copperleaf.ballast.BallastInterceptor
import com.copperleaf.ballast.BallastInterceptorScope
import com.copperleaf.ballast.BallastNotification
import com.copperleaf.ballast.analytics.AnalyticsInterceptor
import com.copperleaf.ballast.awaitViewModelStart
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

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
