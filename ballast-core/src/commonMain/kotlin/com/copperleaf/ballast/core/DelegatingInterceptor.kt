package com.copperleaf.ballast.core

import com.copperleaf.ballast.BallastInterceptor

public class DelegatingInterceptor<Inputs : Any, Events : Any, State : Any>(
    private vararg val delegates: BallastInterceptor<Inputs, Events, State>,
) : BallastInterceptor<Inputs, Events, State> {

// Inputs
// ---------------------------------------------------------------------------------------------------------------------

    override suspend fun onInputAccepted(input: Inputs) {
        delegates.forEach { it.onInputAccepted(input) }
    }

    override suspend fun onInputRejected(input: Inputs) {
        delegates.forEach { it.onInputRejected(input) }
    }

    override fun onInputDropped(input: Inputs) {
        delegates.forEach { it.onInputDropped(input) }
    }

    override suspend fun onInputHandledSuccessfully(input: Inputs) {
        delegates.forEach { it.onInputHandledSuccessfully(input) }
    }

    override suspend fun onInputCancelled(input: Inputs) {
        delegates.forEach { it.onInputCancelled(input) }
    }

    override suspend fun onInputHandlerError(input: Inputs, exception: Throwable) {
        delegates.forEach { it.onInputHandlerError(input, exception) }
    }

// Events
// ---------------------------------------------------------------------------------------------------------------------

    override suspend fun onEventEmitted(event: Events) {
        delegates.forEach { it.onEventEmitted(event) }
    }

    override suspend fun onEventHandlerError(event: Events, exception: Throwable) {
        delegates.forEach { it.onEventHandlerError(event, exception) }
    }

    override fun onEventProcessingStarted() {
        delegates.forEach { it.onEventProcessingStarted() }
    }

    override fun onEventProcessingStopped() {
        delegates.forEach { it.onEventProcessingStopped() }
    }

// States
// ---------------------------------------------------------------------------------------------------------------------

    override suspend fun onStateEmitted(state: State) {
        delegates.forEach { it.onStateEmitted(state) }
    }

// Other
// ---------------------------------------------------------------------------------------------------------------------

    override fun onUnhandledError(exception: Throwable) {
        delegates.forEach { it.onUnhandledError(exception) }
    }
}
