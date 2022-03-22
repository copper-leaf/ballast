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

        logger.debug(notification.toString())
        if (error != null) {
            logger.error(error)
        }
    }
}
