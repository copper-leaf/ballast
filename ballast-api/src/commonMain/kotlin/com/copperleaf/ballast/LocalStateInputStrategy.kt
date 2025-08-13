package com.copperleaf.ballast

import com.copperleaf.ballast.core.FifoInputStrategy
import com.copperleaf.ballast.core.LifoInputStrategy
import com.copperleaf.ballast.core.ParallelInputStrategy
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.StateFlow

/**
 * Ballast ViewModels are designed to be safe and prevent you from doing things that could cause hard-to-debug race
 * conditions and break the purity of the MVI "state machine". But there are several ways to do this safely, though each
 * has their own set of pros/cons. By providing a different InputStrategy to your Ballast ViewModels, you can choose
 * which set of tradeoffs you are willing to accept, or you can define your own strategy customized to your needs.
 *
 * See the following links for the available core input strategies. By default, [LifoInputStrategy], which is suitable
 * for UI-bound ViewModels.
 *
 * @see [LifoInputStrategy]
 * @see [FifoInputStrategy]
 * @see [ParallelInputStrategy]
 */
public interface LocalStateInputStrategy<Inputs : Any, Events : Any, State : Any> :
    InputStrategy<Inputs, Events, State> {

    public interface Guardian<State> : InputStrategy.Guardian {
        public fun observeState(): StateFlow<State>

        public suspend fun getCurrentState(): State

        public suspend fun setState(state: State, deferred: CompletableDeferred<Unit>?)

        public suspend fun updateState(block: (State) -> State)

        public suspend fun updateStateAndGet(block: (State) -> State): State

        public suspend fun getAndUpdateState(block: (State) -> State): State
    }
}
