package com.copperleaf.ballast.core

import com.copperleaf.ballast.BallastInterceptor

public class FilterInterceptor<Inputs : Any, Events : Any, State : Any>(
    private val delegate: BallastInterceptor<Inputs, Events, State>,
    private val filterInputs: (Inputs) -> Boolean = { true },
    private val filterEvents: (Events) -> Boolean = { true },
    private val filterState: (State) -> Boolean = { true },
) : BallastInterceptor<Inputs, Events, State> {

    override suspend fun onInputAccepted(input: Inputs) {
        if (filterInputs(input)) {
            delegate.onInputAccepted(input)
        }
    }

    override suspend fun onInputRejected(input: Inputs) {
        if (filterInputs(input)) {
            delegate.onInputRejected(input)
        }
    }

    override fun onInputDropped(input: Inputs) {
        if (filterInputs(input)) {
            delegate.onInputDropped(input)
        }
    }

    override suspend fun onEventEmitted(event: Events) {
        if (filterEvents(event)) {
            delegate.onEventEmitted(event)
        }
    }

    override suspend fun onStateEmitted(state: State) {
        if (filterState(state)) {
            delegate.onStateEmitted(state)
        }
    }

    override suspend fun onInputHandlerError(input: Inputs, exception: Throwable) {
        if (filterInputs(input)) {
            delegate.onInputHandlerError(input, exception)
        }
    }

    override suspend fun onEventHandlerError(event: Events, exception: Throwable) {
        if (filterEvents(event)) {
            delegate.onEventHandlerError(event, exception)
        }
    }

    override fun onUnhandledError(exception: Throwable) {
        delegate.onUnhandledError(exception)
    }
}
