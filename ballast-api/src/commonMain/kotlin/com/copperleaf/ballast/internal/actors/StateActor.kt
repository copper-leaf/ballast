package com.copperleaf.ballast.internal.actors

import com.copperleaf.ballast.BallastNotification
import com.copperleaf.ballast.BallastViewModelConfiguration
import com.copperleaf.ballast.internal.BallastViewModelImpl
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.updateAndGet

internal class StateActor<Inputs : Any, Events : Any, State : Any>(
    private val impl: BallastViewModelImpl<Inputs, Events, State>
) : BallastViewModelConfiguration<Inputs, Events, State> by impl {
    private val viewModelState: MutableStateFlow<State> = MutableStateFlow(initialState)

    internal suspend fun getCurrentState(): State {
        return viewModelState.value
    }

    internal fun observeStates(): StateFlow<State> {
        return viewModelState.asStateFlow()
    }

    internal suspend fun safelySetState(state: State, deferred: CompletableDeferred<Unit>?) {
        impl.coordinator.coordinatorState.value.checkStateChangeOpen()
        viewModelState.value = state
        impl.interceptorActor.notify(BallastNotification.StateChanged(impl.type, impl.name, getCurrentState()))
        deferred?.complete(Unit)
    }

    internal suspend fun safelyUpdateState(block: (State) -> State) {
        impl.coordinator.coordinatorState.value.checkStateChangeOpen()
        viewModelState.update(block)
        impl.interceptorActor.notify(BallastNotification.StateChanged(impl.type, impl.name, getCurrentState()))
    }

    internal suspend fun safelyUpdateStateAndGet(block: (State) -> State): State {
        impl.coordinator.coordinatorState.value.checkStateChangeOpen()
        return viewModelState.updateAndGet(block).also {
            impl.interceptorActor.notify(BallastNotification.StateChanged(impl.type, impl.name, getCurrentState()))
        }
    }

    internal suspend fun safelyGetAndUpdateState(block: (State) -> State): State {
        impl.coordinator.coordinatorState.value.checkStateChangeOpen()
        return viewModelState.getAndUpdate(block).also {
            impl.interceptorActor.notify(BallastNotification.StateChanged(impl.type, impl.name, getCurrentState()))
        }
    }
}
