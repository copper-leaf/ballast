package com.copperleaf.ballast.crashreporting

import com.copperleaf.ballast.BallastInterceptor
import com.copperleaf.ballast.BallastInterceptorScope
import com.copperleaf.ballast.BallastNotification
import com.copperleaf.ballast.awaitViewModelStart
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

public class CrashReportingInterceptor<Inputs : Any, Events : Any, State : Any>(
    private val crashReporter: CrashReporter,
    private val shouldTrackInput: (Inputs) -> Boolean,
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
                            if (shouldTrackInput(notification.input)) {
                                crashReporter.logInput(
                                    notification.viewModelName,
                                    notification.input,
                                )
                            }
                            Unit
                        }

                        is BallastNotification.InputHandlerError -> {
                            crashReporter.recordInputError(
                                notification.viewModelName,
                                notification.input,
                                notification.throwable,
                            )
                        }

                        is BallastNotification.EventHandlerError -> {
                            crashReporter.recordEventError(
                                notification.viewModelName,
                                notification.event,
                                notification.throwable,
                            )
                        }

                        is BallastNotification.SideJobError -> {
                            crashReporter.recordSideJobError(
                                notification.viewModelName,
                                notification.key,
                                notification.throwable,
                            )
                        }

                        is BallastNotification.UnhandledError -> {
                            crashReporter.recordUnhandledError(
                                notification.viewModelName,
                                notification.throwable,
                            )
                        }

                        else -> {}
                    }
                }
                .collect()
        }
    }

    override fun toString(): String {
        return "CrashReportingInterceptor(crashReporter=$crashReporter)"
    }
}
