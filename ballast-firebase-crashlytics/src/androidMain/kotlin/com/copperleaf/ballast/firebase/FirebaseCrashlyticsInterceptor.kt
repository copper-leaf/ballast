package com.copperleaf.ballast.firebase

import com.copperleaf.ballast.BallastInterceptor
import com.copperleaf.ballast.BallastLogger
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
        const val SideJobKey = "SideJobKey"
        const val ExceptionType = "ExceptionType"
    }

    override suspend fun onNotify(logger: BallastLogger, notification: BallastNotification<Inputs, Events, State>) {
        when (notification) {
            is BallastNotification.InputAccepted -> {
                if (!notification.input.isAnnotatedWith(FirebaseCrashlyticsIgnore::class)) {
                    crashlytics.setCustomKeys {
                        key(Keys.ViewModelName, notification.viewModelName)
                        key(Keys.InputType, "${notification.viewModelName}.${notification.input::class.java.simpleName}")
                    }
                    crashlytics.log("${notification.viewModelName}.${notification.input}")
                }
            }

            is BallastNotification.InputHandlerError -> {
                onError(notification, "Input", notification.throwable, true) {
                    key(Keys.InputType, "${notification.viewModelName}.${notification.input::class.java.simpleName}")
                }
            }
            is BallastNotification.EventHandlerError -> {
                onError(notification, "Event", notification.throwable, true) {
                    key(Keys.EventType, "${notification.viewModelName}.${notification.event::class.java.simpleName}")
                }
            }
            is BallastNotification.SideJobError -> {
                onError(notification, "SideJob", notification.throwable, true) {
                    key(Keys.SideJobKey, "${notification.viewModelName}.${notification.key}")
                }
            }
            is BallastNotification.UnhandledError -> {
                onError(notification, "Unknown", notification.throwable, false) {
                }
            }
            else -> {}
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
            key(Keys.ViewModelName, notification.viewModelName)
            key(Keys.ExceptionType, type)
            extraKeys()
        }
        crashlytics.recordException(BallastException(throwable, handled, "[redacted]", emptyList()))
    }

    private fun <T : Any, Ann : Annotation> T.isAnnotatedWith(annotationClass: KClass<Ann>): Boolean {
        return this::class.java.isAnnotationPresent(annotationClass.java)
    }
}
