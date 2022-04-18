package com.copperleaf.ballast.core

import com.copperleaf.ballast.BallastInterceptor
import com.copperleaf.ballast.BallastLogger
import com.copperleaf.ballast.BallastNotification

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

    override suspend fun onNotify(logger: BallastLogger, notification: BallastNotification<Inputs, Events, State>) {
        val error: BallastException? = when (notification) {
            is BallastNotification.StateChanged -> {
                latestState = notification.state
                null
            }
            is BallastNotification.InputAccepted -> {
                inputSequence += notification.input
                null
            }

            is BallastNotification.InputHandlerError -> {
                BallastException(notification.throwable, true, latestState, inputSequence)
            }
            is BallastNotification.EventHandlerError -> {
                BallastException(notification.throwable, true, latestState, inputSequence)
            }
            is BallastNotification.SideJobError -> {
                BallastException(notification.throwable, true, latestState, inputSequence)
            }
            is BallastNotification.UnhandledError -> {
                BallastException(notification.throwable, false, latestState, inputSequence)
            }

            else -> {
                null
            }
        }

        val loggerFn: (String) -> Unit = when (notification) {
            is BallastNotification.ViewModelStarted -> logger::debug
            is BallastNotification.ViewModelCleared -> logger::debug

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

            is BallastNotification.UnhandledError -> logger::info
        }

        loggerFn(notification.toString())
        if (error != null) {
            logger.error(error)
        }
    }
}
