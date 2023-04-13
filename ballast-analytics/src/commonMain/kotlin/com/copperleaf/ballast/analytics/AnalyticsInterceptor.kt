package com.copperleaf.ballast.analytics

import com.copperleaf.ballast.BallastInterceptor
import com.copperleaf.ballast.BallastInterceptorScope
import com.copperleaf.ballast.BallastNotification
import com.copperleaf.ballast.awaitViewModelStart
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

public class AnalyticsInterceptor<Inputs : Any, Events : Any, State : Any>(
    private val tracker: AnalyticsTracker,
    private val shouldTrackInput: (Inputs) -> Boolean,
) : BallastInterceptor<Inputs, Events, State> {

    private object Keys {
        const val ViewModelName = "ViewModelName"
        const val InputType = "InputType"
        const val InputValue = "InputValue"
    }

    override fun BallastInterceptorScope<Inputs, Events, State>.start(
        notifications: Flow<BallastNotification<Inputs, Events, State>>
    ) {
        launch(start = CoroutineStart.UNDISPATCHED) {
            notifications.awaitViewModelStart()
            notifications
                .onEach { notification ->
                    if (notification is BallastNotification.InputAccepted) {
                        if (shouldTrackInput(notification.input)) {
                            tracker.trackAnalyticsEvent(
                                "action",
                                mapOf(
                                    Keys.ViewModelName to notification.viewModelName,
                                    Keys.InputType to "${notification.viewModelName}.${notification.input::class.simpleName}",
                                    Keys.InputValue to "${notification.viewModelName}.${notification.input}",
                                )
                            )
                        }
                    }
                }
                .collect()
        }
    }
}
