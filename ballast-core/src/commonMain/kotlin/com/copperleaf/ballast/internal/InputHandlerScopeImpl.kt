package com.copperleaf.ballast.internal

import com.copperleaf.ballast.BallastLogger
import com.copperleaf.ballast.InputHandlerScope
import com.copperleaf.ballast.InputStrategy
import com.copperleaf.ballast.SideJobScope

internal class InputHandlerScopeImpl<Inputs : Any, Events : Any, State : Any>(
    private val guardian: InputStrategy.Guardian,
    private val impl: BallastViewModelImpl<Inputs, Events, State>,
) : InputHandlerScope<Inputs, Events, State> {

    override val logger: BallastLogger get() = impl.logger

    override suspend fun getCurrentState(): State {
        guardian.checkStateAccess()
        return impl.getCurrentState()
    }

    override suspend fun updateState(block: (State) -> State) {
        guardian.checkStateUpdate()
        impl.safelyUpdateState(block)
    }

    override suspend fun updateStateAndGet(block: (State) -> State): State {
        guardian.checkStateUpdate()
        return impl.safelyUpdateStateAndGet(block)
    }

    override suspend fun getAndUpdateState(block: (State) -> State): State {
        guardian.checkStateUpdate()
        return impl.safelyGetAndUpdateState(block)
    }

    override suspend fun postEvent(event: Events) {
        guardian.checkPostEvent()
        impl.enqueueEvent(event, null, false)
    }

    override fun sideJob(
        key: String,
        block: suspend SideJobScope<Inputs, Events, State>.() -> Unit
    ) {
        guardian.checkSideJob()
        impl.enqueueSideJob(key, block)
    }

    override fun noOp() {
        guardian.checkNoOp()
    }

    internal fun close() {
        guardian.close()
    }
}
