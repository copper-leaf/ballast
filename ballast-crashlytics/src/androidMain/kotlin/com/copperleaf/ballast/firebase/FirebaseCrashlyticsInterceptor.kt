package com.copperleaf.ballast.firebase

import com.copperleaf.ballast.BallastInterceptor
import com.copperleaf.ballast.BallastNotification
import com.copperleaf.ballast.core.BallastException
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.crashlytics.ktx.KeyValueBuilder
import com.google.firebase.crashlytics.ktx.setCustomKeys
import kotlin.reflect.KClass

class FirebaseCrashlyticsInterceptor<Inputs : Any, Events : Any, State : Any>(
    private val crashlytics: FirebaseCrashlytics,
) : BallastInterceptor<Inputs, Events, State> {

    private object Keys {
        const val ViewModelName = "ViewModelName"
        const val InputType = "InputType"
        const val EventType = "EventType"
        const val SideEffectKey = "SideEffectKey"
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

    override suspend fun onNotify(notification: BallastNotification<Inputs, Events, State>) {
        when (notification) {
            is BallastNotification.InputAccepted -> {
                if (!notification.input.isAnnotatedWith(Ignore::class)) {
                    crashlytics.setCustomKeys {
                        key(Keys.ViewModelName, notification.vm.name)
                        key(Keys.InputType, "${notification.vm.name}.${notification.input::class.java.simpleName}")
                    }
                    crashlytics.log("${notification.vm.name}.${notification.input}")
                }
            }

            is BallastNotification.InputHandlerError -> {
                onError(notification, "Input", notification.throwable, true) {
                    key(Keys.InputType, "${notification.vm.name}.${notification.input::class.java.simpleName}")
                }
            }
            is BallastNotification.EventHandlerError -> {
                onError(notification, "Event", notification.throwable, true) {
                    key(Keys.EventType, "${notification.vm.name}.${notification.event::class.java.simpleName}")
                }
            }
            is BallastNotification.SideEffectError -> {
                onError(notification, "SideEffect", notification.throwable, true) {
                    key(Keys.SideEffectKey, "${notification.vm.name}.${notification.key}")
                }
            }
            is BallastNotification.UnhandledError -> {
                onError(notification, "Unknown", notification.throwable, false) {
                }
            }
            else -> { }
        }
    }

    private fun onError(
        notification: BallastNotification<Inputs, Events, State>,
        type: String,
        throwable: Throwable,
        handled: Boolean,
        extraKeys: KeyValueBuilder.() -> Unit,
    ) {
        crashlytics.setCustomKeys {
            key(Keys.ViewModelName, notification.vm.name)
            key(Keys.ExceptionType, type)
            extraKeys()
        }
        crashlytics.recordException(BallastException(throwable, handled, "[redacted]", emptyList()))
    }

    private fun <T : Any, Ann : Annotation> T.isAnnotatedWith(annotationClass: KClass<Ann>): Boolean {
        return this::class.java.isAnnotationPresent(annotationClass.java)
    }
}
