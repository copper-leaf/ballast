package com.copperleaf.ballast.core

import com.copperleaf.ballast.BallastInterceptor
import com.copperleaf.ballast.BallastInterceptorScope
import com.copperleaf.ballast.BallastNotification
import com.copperleaf.ballast.BallastViewModelConfiguration
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

/**
 * [LoggingInterceptor] will print all Ballast activity to the logger provided in the [BallastViewModelConfiguration],
 * for debugging purposes. The information logged by this interceptor may be quite verbose, but it can be really handy
 * for quickly inspecting the data in your ViewModel and determining what happened and in what order.
 *
 * This Interceptor should never be used in production as it is likely to leak sensitive information to the logs. It is
 * not designed to make a "paper trail" for production logging. You should use the
 * [Ballast Firebase Analytics](https://copper-leaf.github.io/ballast/wiki/modules/ballast-firebase/#analytics) for that
 * on Android, or build a customer logger suitable for production that can ensure nothing sensitive gets logged.
 */
public class LoggingInterceptor<Inputs : Any, Events : Any, State : Any> : BallastInterceptor<Inputs, Events, State> {

    /**
     * A trivial implementation of an error log, which is attached to errors reported to crash reporters. A more robust
     * method would be to write the input sequence to a File, which is then uploaded as an attachment File to the error
     * report.
     */
    private val inputSequence = mutableListOf<Inputs>()

    /**
     * Also save the latest state that was emitted (if any) to aid in reproducing reported issues.
     */
    private var latestState: State? = null

    override fun BallastInterceptorScope<Inputs, Events, State>.start(
        notifications: Flow<BallastNotification<Inputs, Events, State>>,
    ) {
        launch(start = CoroutineStart.UNDISPATCHED) {
            notifications
                .onEach { notification ->
                    val error: BallastLoggingException? = when (notification) {
                        is BallastNotification.StateChanged -> {
                            latestState = notification.state
                            null
                        }

                        is BallastNotification.InputAccepted -> {
                            inputSequence += notification.input
                            null
                        }

                        is BallastNotification.InputHandlerError -> {
                            BallastLoggingException(notification.throwable, true, latestState, inputSequence)
                        }

                        is BallastNotification.EventHandlerError -> {
                            BallastLoggingException(notification.throwable, true, latestState, inputSequence)
                        }

                        is BallastNotification.SideJobError -> {
                            BallastLoggingException(notification.throwable, true, latestState, inputSequence)
                        }

                        is BallastNotification.UnhandledError -> {
                            BallastLoggingException(notification.throwable, false, latestState, inputSequence)
                        }

                        else -> {
                            null
                        }
                    }

                    val loggerFn: (String) -> Unit = when (notification) {
                        is BallastNotification.ViewModelStatusChanged -> logger::debug

                        is BallastNotification.InputQueued -> logger::debug
                        is BallastNotification.InputAccepted -> logger::info
                        is BallastNotification.InputRejected -> logger::info
                        is BallastNotification.InputDropped -> logger::info
                        is BallastNotification.InputHandledSuccessfully -> logger::debug
                        is BallastNotification.InputCancelled -> logger::info
                        is BallastNotification.InputHandlerError -> logger::info

                        is BallastNotification.EventQueued -> logger::debug
                        is BallastNotification.EventEmitted -> logger::info
                        is BallastNotification.EventHandledSuccessfully -> logger::debug
                        is BallastNotification.EventHandlerError -> logger::info
                        is BallastNotification.EventProcessingStarted -> logger::debug
                        is BallastNotification.EventProcessingStopped -> logger::debug

                        is BallastNotification.StateChanged -> logger::info

                        is BallastNotification.SideJobQueued -> logger::debug
                        is BallastNotification.SideJobStarted -> logger::info
                        is BallastNotification.SideJobCompleted -> logger::debug
                        is BallastNotification.SideJobCancelled -> logger::info
                        is BallastNotification.SideJobError -> logger::info

                        is BallastNotification.InterceptorAttached -> logger::debug
                        is BallastNotification.InterceptorFailed -> logger::debug

                        is BallastNotification.UnhandledError -> logger::info
                    }

                    loggerFn(notification.toString())
                    if (error != null) {
                        logger.error(error)
                    }
                }
                .collect()
        }
    }
}
