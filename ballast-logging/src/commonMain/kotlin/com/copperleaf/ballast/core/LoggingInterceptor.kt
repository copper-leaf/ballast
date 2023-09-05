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
 * [Ballast Analytics](https://copper-leaf.github.io/ballast/wiki/modules/ballast-analytics) for that
 * on Android, or build a customer logger suitable for production that can ensure nothing sensitive gets logged.
 */
public class LoggingInterceptor<Inputs : Any, Events : Any, State : Any>(
    private val logDebug: Boolean = true,
    private val logInfo: Boolean = true,
    private val logError: Boolean = true,
) : BallastInterceptor<Inputs, Events, State> {

    override fun BallastInterceptorScope<Inputs, Events, State>.start(
        notifications: Flow<BallastNotification<Inputs, Events, State>>,
    ) {
        launch(start = CoroutineStart.UNDISPATCHED) {
            // A trivial implementation of an error log, which is attached to errors reported to crash reporters. A more
            // robust method would be to write the input sequence to a File, which is then uploaded as an attachment
            // File to the error report.
            val inputSequence = mutableListOf<Inputs>()

            // Also save the latest state that was emitted (if any) to aid in reproducing reported issues.
            var latestState: State? = null

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

                    val loggerFn: ((String) -> Unit)? = when (notification) {
                        is BallastNotification.ViewModelStatusChanged -> logger::debug.takeIf { logDebug }

                        is BallastNotification.InputQueued -> logger::debug.takeIf { logDebug }
                        is BallastNotification.InputAccepted -> logger::info.takeIf { logInfo }
                        is BallastNotification.InputRejected -> logger::info.takeIf { logInfo }
                        is BallastNotification.InputDropped -> logger::info.takeIf { logInfo }
                        is BallastNotification.InputHandledSuccessfully -> logger::debug.takeIf { logDebug }
                        is BallastNotification.InputCancelled -> logger::info.takeIf { logInfo }
                        is BallastNotification.InputHandlerError -> logger::info.takeIf { logInfo }

                        is BallastNotification.EventQueued -> logger::debug.takeIf { logDebug }
                        is BallastNotification.EventEmitted -> logger::info.takeIf { logInfo }
                        is BallastNotification.EventHandledSuccessfully -> logger::debug.takeIf { logDebug }
                        is BallastNotification.EventHandlerError -> logger::info.takeIf { logInfo }
                        is BallastNotification.EventProcessingStarted -> logger::debug.takeIf { logDebug }
                        is BallastNotification.EventProcessingStopped -> logger::debug.takeIf { logDebug }

                        is BallastNotification.StateChanged -> logger::info.takeIf { logInfo }

                        is BallastNotification.SideJobQueued -> logger::debug.takeIf { logDebug }
                        is BallastNotification.SideJobStarted -> logger::info.takeIf { logInfo }
                        is BallastNotification.SideJobCompleted -> logger::debug.takeIf { logDebug }
                        is BallastNotification.SideJobCancelled -> logger::info.takeIf { logInfo }
                        is BallastNotification.SideJobError -> logger::info.takeIf { logInfo }

                        is BallastNotification.InterceptorAttached -> logger::debug.takeIf { logDebug }
                        is BallastNotification.InterceptorFailed -> logger::debug.takeIf { logDebug }

                        is BallastNotification.UnhandledError -> logger::info.takeIf { logInfo }
                    }

                    loggerFn?.invoke(notification.toString())
                    if (error != null && logError) {
                        logger.error(error)
                    }
                }
                .collect()
        }
    }

    override fun toString(): String {
        val enabled = buildList<String> {
            if(logDebug) { this += "debug" }
            if(logInfo) { this += "info" }
            if(logError) { this += "error" }
        }
        return "LoggingInterceptor(enabled=$enabled)"
    }
}
