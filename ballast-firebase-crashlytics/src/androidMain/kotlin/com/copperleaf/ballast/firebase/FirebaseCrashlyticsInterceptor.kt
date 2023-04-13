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

@Deprecated(
    "Use CrashReportingInterceptor with FirebaseCrashReporter instead. Deprecated since v3, to be removed in v4.",
    replaceWith = ReplaceWith(
        "CrashReportingInterceptor(FirebaseCrashReporter(Firebase.crashlytics)) { !it.isAnnotatedWith<FirebaseCrashlyticsIgnore>() }",

        "com.copperleaf.ballast.crashreporting.CrashReportingInterceptor",
        "com.google.firebase.ktx.Firebase",
        "com.google.firebase.crashlytics.ktx.crashlytics",
        "com.copperleaf.ballast.firebase.FirebaseCrashReporter",
        "com.copperleaf.ballast.firebase.FirebaseCrashlyticsIgnore",
    )
)
public class FirebaseCrashlyticsInterceptor<Inputs : Any, Events : Any, State : Any>(
    private val crashlytics: FirebaseCrashlytics,
) : BallastInterceptor<Inputs, Events, State> {

    override fun BallastInterceptorScope<Inputs, Events, State>.start(
        notifications: Flow<BallastNotification<Inputs, Events, State>>,
    ) {
        launch(start = CoroutineStart.UNDISPATCHED) {
            notifications.awaitViewModelStart()
            notifications
                .onEach { notification ->
                    when (notification) {
                        is BallastNotification.InputAccepted -> {
                            if (!notification.input.isAnnotatedWith<FirebaseCrashlyticsIgnore>()) {
                                crashlytics.setCustomKeys {
                                    key(FirebaseCrashReporter.Keys.ViewModelName, notification.viewModelName)
                                    key(
                                        FirebaseCrashReporter.Keys.InputType,
                                        "${notification.viewModelName}.${notification.input::class.java.simpleName}"
                                    )
                                }
                                crashlytics.log("${notification.viewModelName}.${notification.input}")
                            }
                        }

                        is BallastNotification.InputHandlerError -> {
                            onError(notification, "Input", notification.throwable, true) {
                                key(FirebaseCrashReporter.Keys.InputType, "${notification.viewModelName}.${notification.input::class.java.simpleName}")
                            }
                        }

                        is BallastNotification.EventHandlerError -> {
                            onError(notification, "Event", notification.throwable, true) {
                                key(FirebaseCrashReporter.Keys.EventType, "${notification.viewModelName}.${notification.event::class.java.simpleName}")
                            }
                        }

                        is BallastNotification.SideJobError -> {
                            onError(notification, "SideJob", notification.throwable, true) {
                                key(FirebaseCrashReporter.Keys.SideJobKey, "${notification.viewModelName}.${notification.key}")
                            }
                        }

                        is BallastNotification.UnhandledError -> {
                            onError(notification, "Unknown", notification.throwable, false) {
                            }
                        }

                        else -> {}
                    }
                }
                .collect()
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
            key(FirebaseCrashReporter.Keys.ViewModelName, notification.viewModelName)
            key(FirebaseCrashReporter.Keys.ExceptionType, type)
            extraKeys()
        }
        crashlytics.recordException(BallastCrashlyticsException(throwable, handled))
    }
}

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
