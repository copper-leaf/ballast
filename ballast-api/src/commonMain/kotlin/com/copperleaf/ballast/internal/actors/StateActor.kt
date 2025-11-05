package com.copperleaf.ballast.internal.actors

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.StateFlow

public interface StateActor<Inputs : Any, Events : Any, State : Any> {

    public suspend fun getCurrentState(): State

    public fun observeStates(): StateFlow<State>

    public suspend fun safelySetState(state: State, deferred: CompletableDeferred<Unit>?)

    public suspend fun safelyUpdateState(block: (State) -> State)

    public suspend fun safelyUpdateStateAndGet(block: (State) -> State): State

    public suspend fun safelyGetAndUpdateState(block: (State) -> State): State
}
