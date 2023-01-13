package com.copperleaf.ballast.firebase

import com.copperleaf.ballast.BallastInterceptor
import com.copperleaf.ballast.BallastInterceptorScope
import com.copperleaf.ballast.BallastNotification
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
import kotlin.reflect.KClass

@Deprecated(
    "Use BallastAnalyticsInterceptor with FirebaseAnalyticsTracker instead. Deprecated since v3, to be removed in v4.",
    replaceWith = ReplaceWith(
        "BallastAnalyticsInterceptor(FirebaseAnalyticsTracker(Firebase.analytics)) { it.isAnnotatedWith(FirebaseAnalyticsTrackInput::class) }",

        "com.copperleaf.ballast.firebase.BallastAnalyticsInterceptor",
        "com.google.firebase.ktx.Firebase",
        "com.google.firebase.analytics.ktx.analytics",
        "com.copperleaf.ballast.firebase.FirebaseAnalyticsTracker",
        "com.copperleaf.ballast.firebase.FirebaseAnalyticsTrackInput",
    )
)
class FirebaseAnalyticsInterceptor<Inputs : Any, Events : Any, State : Any>(
    private val analytics: FirebaseAnalytics,
) : BallastInterceptor<Inputs, Events, State> {

    private object Keys {
        const val ViewModelName = "ViewModelName"
        const val InputType = "InputType"
        const val InputValue = "InputValue"
    }

    override fun BallastInterceptorScope<Inputs, Events, State>.start(
        notifications: Flow<BallastNotification<Inputs, Events, State>>,
    ) {
        launch(start = CoroutineStart.UNDISPATCHED) {
            notifications.awaitViewModelStart()
            notifications
                .onEach { notification ->
                    if (notification is BallastNotification.InputAccepted) {
                        if (notification.input.isAnnotatedWith(FirebaseAnalyticsTrackInput::class)) {
                            analytics.logEvent("action") {
                                param(Keys.ViewModelName, notification.viewModelName)
                                param(
                                    Keys.InputType,
                                    "${notification.viewModelName}.${notification.input::class.java.simpleName}"
                                )
                                param(Keys.InputValue, "${notification.viewModelName}.${notification.input}")
                            }
                        }
                    }
                }
                .collect()
        }
    }
}

public fun <Inputs : Any, Events : Any, State : Any> FirebaseAnalyticsInterceptor(
    analytics: FirebaseAnalytics = Firebase.analytics,
    shouldTrackInput: (Inputs) -> Boolean = { it.isAnnotatedWith(FirebaseAnalyticsTrackInput::class) },
): BallastAnalyticsInterceptor<Inputs, Events, State> {
    return BallastAnalyticsInterceptor(
        tracker = FirebaseAnalyticsTracker(analytics),
        shouldTrackInput = shouldTrackInput,
    )
}

public fun <T : Any, Ann : Annotation> T.isAnnotatedWith(annotationClass: KClass<Ann>): Boolean {
    return this::class.java.isAnnotationPresent(annotationClass.java)
}
