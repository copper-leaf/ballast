package com.copperleaf.ballast.internal.actors

import com.copperleaf.ballast.BallastViewModelConfiguration
import com.copperleaf.ballast.internal.BallastViewModelImpl
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.StateFlow

internal class LocalStateActor<Inputs : Any, Events : Any, State : Any>(
    private val impl: BallastViewModelImpl<Inputs, Events, State>
) : BallastViewModelConfiguration<Inputs, Events, State> by impl,
    StateActor<Inputs, Events, State> {

    override suspend fun getCurrentState(): State {
        error("ViewModels with LocalStateInputStrategy do not have externally-visible state (getCurrentState)")
    }

    override fun observeStates(): StateFlow<State> {
        error("ViewModels with LocalStateInputStrategy do not have externally-visible state (observeStates)")
    }

    override suspend fun safelySetState(state: State, deferred: CompletableDeferred<Unit>?) {
        error("ViewModels with LocalStateInputStrategy do not have externally-visible state (safelySetState)")
    }

    override suspend fun safelyUpdateState(block: (State) -> State) {
        error("ViewModels with LocalStateInputStrategy do not have externally-visible state (safelyUpdateState)")
    }

    override suspend fun safelyUpdateStateAndGet(block: (State) -> State): State {
        error("ViewModels with LocalStateInputStrategy do not have externally-visible state (safelyUpdateStateAndGet)")
    }

    override suspend fun safelyGetAndUpdateState(block: (State) -> State): State {
        error("ViewModels with LocalStateInputStrategy do not have externally-visible state (safelyGetAndUpdateState)")
    }
}
