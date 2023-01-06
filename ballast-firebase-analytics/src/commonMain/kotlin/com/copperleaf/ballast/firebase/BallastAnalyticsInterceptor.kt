package com.copperleaf.ballast.firebase

import com.copperleaf.ballast.BallastInterceptor
import com.copperleaf.ballast.BallastLogger
import com.copperleaf.ballast.BallastNotification

public class BallastAnalyticsInterceptor<Inputs : Any, Events : Any, State : Any>(
    private val tracker: AnalyticsTracker,
    private val shouldTrackInput: (Inputs) -> Boolean,
) : BallastInterceptor<Inputs, Events, State> {

    private object Keys {
        const val ViewModelName = "ViewModelName"
        const val InputType = "InputType"
        const val InputValue = "InputValue"
    }

    override suspend fun onNotify(logger: BallastLogger, notification: BallastNotification<Inputs, Events, State>) {
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
}
