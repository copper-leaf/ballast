package com.copperleaf.ballast.core

import com.copperleaf.ballast.BallastInterceptor
import com.copperleaf.ballast.BallastNotification
import com.copperleaf.ballast.BallastViewModel

public class LoggingInterceptor<Inputs : Any, Events : Any, State : Any>(
    private val logError: BallastViewModel<Inputs, Events, State>.(BallastException) -> Unit = { },
    private val logMessage: BallastViewModel<Inputs, Events, State>.(String) -> Unit = { },
) : BallastInterceptor<Inputs, Events, State> {

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

    override suspend fun onNotify(notification: BallastNotification<Inputs, Events, State>) {
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
            is BallastNotification.SideEffectError -> {
                BallastException(notification.throwable, true, latestState, inputSequence)
            }
            is BallastNotification.UnhandledError -> {
                BallastException(notification.throwable, false, latestState, inputSequence)
            }

            else -> {
                null
            }
        }

        logMessage(notification.vm, notification.toString())
        if (error != null) {
            logError(notification.vm, error)
        }
    }
}
