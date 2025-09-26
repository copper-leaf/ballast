package com.copperleaf.ballast.firebase

import com.copperleaf.ballast.crashreporting.CrashReportingInterceptor
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase

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
