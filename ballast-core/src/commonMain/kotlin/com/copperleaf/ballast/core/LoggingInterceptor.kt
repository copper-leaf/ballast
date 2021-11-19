package com.copperleaf.ballast.core

import com.copperleaf.ballast.BallastInterceptor

public class LoggingInterceptor<Inputs : Any, Events : Any, State : Any>(
    private val logMessage: (String) -> Unit,
    private val logError: (BallastException) -> Unit,
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

    override suspend fun onInputAccepted(input: Inputs) {
        inputSequence.add(input)
        logMessage("Accepting input: $input")
    }

    override suspend fun onInputRejected(input: Inputs) {
        logMessage("Rejecting input: $input")
    }

    override fun onInputDropped(input: Inputs) {
        logMessage("Dropping input: $input")
    }

    override suspend fun onEventEmitted(event: Events) {
        logMessage("Emitting event: $event")
    }

    override suspend fun onStateEmitted(state: State) {
        logMessage("State changed: $state")
        latestState = state
    }

    override suspend fun onInputCancelled(input: Inputs) {
        logMessage("Cancelling input: $input")
    }

    override suspend fun onInputHandlerError(input: Inputs, exception: Throwable) {
        logMessage("Exception handling Input")
        logError(BallastException(exception, true, latestState, inputSequence))
    }

    override suspend fun onEventHandlerError(event: Events, exception: Throwable) {
        logMessage("Exception handling Event")
        logError(BallastException(exception, true, latestState, inputSequence))
    }

    override fun onUnhandledError(exception: Throwable) {
        logMessage("Uncaught Exception")
        logError(BallastException(exception, false, latestState, inputSequence))
    }

    override fun onEventProcessingStarted() {
        logMessage("Event processing started")
    }

    override fun onEventProcessingStopped() {
        logMessage("Event processing stopped")
    }
}
