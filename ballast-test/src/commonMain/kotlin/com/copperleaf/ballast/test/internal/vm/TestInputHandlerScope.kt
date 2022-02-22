package com.copperleaf.ballast.test.internal.vm

import com.copperleaf.ballast.InputHandlerScope
import com.copperleaf.ballast.SideEffectScope

internal class TestInputHandlerScope<Inputs : Any, Events : Any, State : Any>(
    private val inputHandlerScopeDelegate: InputHandlerScope<TestViewModel.Inputs<Inputs>, Events, State>,
) : InputHandlerScope<Inputs, Events, State> {
    override suspend fun getCurrentState(): State {
        return inputHandlerScopeDelegate.getCurrentState()
    }

    override suspend fun updateState(block: (State) -> State) {
        inputHandlerScopeDelegate.updateState(block)
    }

    override suspend fun updateStateAndGet(block: (State) -> State): State {
        return inputHandlerScopeDelegate.updateStateAndGet(block)
    }

    override suspend fun getAndUpdateState(block: (State) -> State): State {
        return inputHandlerScopeDelegate.getAndUpdateState(block)
    }

    override suspend fun postEvent(event: Events) {
        inputHandlerScopeDelegate.postEvent(event)
    }

    override fun sideEffect(
        key: String,
        block: suspend SideEffectScope<Inputs, Events, State>.() -> Unit
    ) {
        inputHandlerScopeDelegate.sideEffect(
            key = key,
            block = { TestSideEffectScope(this).block() }
        )
    }

    override fun noOp() {
        inputHandlerScopeDelegate.noOp()
    }
}
