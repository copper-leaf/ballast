package com.copperleaf.ballast.internal.actors

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.StateFlow

internal interface StateActor<Inputs : Any, Events : Any, State : Any> {

     suspend fun getCurrentState(): State

     fun observeStates(): StateFlow<State>

     suspend fun safelySetState(state: State, deferred: CompletableDeferred<Unit>?)

     suspend fun safelyUpdateState(block: (State) -> State)

     suspend fun safelyUpdateStateAndGet(block: (State) -> State): State

     suspend fun safelyGetAndUpdateState(block: (State) -> State): State
}
