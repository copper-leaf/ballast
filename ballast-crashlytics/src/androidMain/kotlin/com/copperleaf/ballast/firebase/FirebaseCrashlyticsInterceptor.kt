package com.copperleaf.ballast.firebase

import com.copperleaf.ballast.BallastInterceptor
import com.copperleaf.ballast.core.BallastException
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.crashlytics.ktx.setCustomKeys
import kotlin.reflect.KClass

class FirebaseCrashlyticsInterceptor<Inputs : Any, Events : Any, State : Any>(
    private val name: String,
    private val crashlytics: FirebaseCrashlytics,
) : BallastInterceptor<Inputs, Events, State> {

    private object Keys {
        const val ViewModelName = "ViewModelName"
        const val InputType = "InputType"
        const val EventType = "EventType"
        const val ExceptionType = "ExceptionType"
    }

    /**
     * By default, all Inputs are sent to Crashlytics for the "debug log" to help in diagnosing
     * crashes. Some Inputs may occur very frequently but contain little useful information for
     * diagnostics (such as updating TextFields), and can be excluded from the debug log by
     * annotating the class with @Ignore.
     */
    @Retention(AnnotationRetention.RUNTIME)
    @Target(AnnotationTarget.CLASS)
    annotation class Ignore

    override suspend fun onInputAccepted(input: Inputs) {
        if (!input.isAnnotatedWith(Ignore::class)) {
            crashlytics.setCustomKeys {
                key(Keys.ViewModelName, name)
                key(Keys.InputType, "$name.${input::class.java.simpleName}")
            }
            crashlytics.log("$name.$input")
        }
    }

    override suspend fun onInputHandlerError(input: Inputs, exception: Throwable) {
        crashlytics.setCustomKeys {
            key(Keys.ViewModelName, name)
            key(Keys.InputType, "$name.${input::class.java.simpleName}")
            key(Keys.ExceptionType, "Input")
        }
        crashlytics.recordException(BallastException(exception, true, "[redacted]", emptyList()))
    }

    override suspend fun onEventHandlerError(event: Events, exception: Throwable) {
        crashlytics.setCustomKeys {
            key(Keys.ViewModelName, name)
            key(Keys.EventType, "$name.${event::class.java.simpleName}")
            key(Keys.ExceptionType, "Event")
        }
        crashlytics.recordException(BallastException(exception, true, "[redacted]", emptyList()))
    }

    override fun onUnhandledError(exception: Throwable) {
        crashlytics.setCustomKeys {
            key(Keys.ViewModelName, name)
            key(Keys.ExceptionType, "Unknown")
        }
        crashlytics.recordException(BallastException(exception, false, "[redacted]", emptyList()))
    }

    private fun <T : Any, Ann : Annotation> T.isAnnotatedWith(annotationClass: KClass<Ann>): Boolean {
        return this::class.java.isAnnotationPresent(annotationClass.java)
    }
}
