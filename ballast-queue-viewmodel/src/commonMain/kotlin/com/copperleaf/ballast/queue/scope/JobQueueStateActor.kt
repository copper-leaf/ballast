package com.copperleaf.ballast.queue.scope

import com.copperleaf.ballast.internal.actors.StateActor
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.StateFlow

internal class JobQueueStateActor<Inputs : Any, Events : Any, State : Any> : StateActor<Inputs, Events, State> {

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
