package com.copperleaf.ballast.test.internal.vm

import com.copperleaf.ballast.BallastLogger
import com.copperleaf.ballast.InputHandlerScope
import com.copperleaf.ballast.SideJobScope

internal class TestInputHandlerScope<Inputs : Any, Events : Any, State : Any>(
    private val inputHandlerScopeDelegate: InputHandlerScope<TestViewModel.Inputs<Inputs>, Events, State>,
) : InputHandlerScope<Inputs, Events, State> {

    override val logger: BallastLogger
        get() = inputHandlerScopeDelegate.logger

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

    override fun sideJob(
        key: String,
        block: suspend SideJobScope<Inputs, Events, State>.() -> Unit
    ) {
        inputHandlerScopeDelegate.sideJob(
            key = key,
            block = { TestSideJobScope(key, this).block() }
        )
    }

    override fun cancelSideJob(key: String) {
        inputHandlerScopeDelegate.cancelSideJob(key)
    }

    override fun noOp() {
        inputHandlerScopeDelegate.noOp()
    }
}
