package com.copperleaf.ballast.firebase

import com.copperleaf.ballast.BallastInterceptor
import com.copperleaf.ballast.BallastInterceptorScope
import com.copperleaf.ballast.BallastNotification
import com.copperleaf.ballast.awaitViewModelStart
import com.copperleaf.ballast.crashreporting.CrashReportingInterceptor
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.crashlytics.ktx.KeyValueBuilder
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.crashlytics.ktx.setCustomKeys
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

public fun <Inputs : Any, Events : Any, State : Any> FirebaseCrashlyticsInterceptor(
    crashlytics: FirebaseCrashlytics = Firebase.crashlytics,
    shouldTrackInput: (Inputs) -> Boolean = { !it.isAnnotatedWith<FirebaseCrashlyticsIgnore>() },
): CrashReportingInterceptor<Inputs, Events, State> {
    return CrashReportingInterceptor(
        crashReporter = FirebaseCrashReporter(crashlytics),
        shouldTrackInput = shouldTrackInput,
    )
}

public inline fun <reified Ann : Annotation> Any.isAnnotatedWith(): Boolean {
    return this::class.java.isAnnotationPresent(Ann::class.java)
}
