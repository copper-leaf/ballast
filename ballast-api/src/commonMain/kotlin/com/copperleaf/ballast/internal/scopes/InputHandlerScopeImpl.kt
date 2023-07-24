package com.copperleaf.ballast.internal.scopes

import com.copperleaf.ballast.BallastLogger
import com.copperleaf.ballast.InputHandlerScope
import com.copperleaf.ballast.InputStrategy
import com.copperleaf.ballast.SideJobScope
import com.copperleaf.ballast.internal.BallastViewModelImpl
import com.copperleaf.ballast.internal.actors.EventActor
import com.copperleaf.ballast.internal.actors.SideJobActor
import com.copperleaf.ballast.internal.actors.StateActor

internal class InputHandlerScopeImpl<Inputs : Any, Events : Any, State : Any>(
    private val guardian: InputStrategy.Guardian,
    private val impl: BallastViewModelImpl<Inputs, Events, State>,

    private val stateActor: StateActor<Inputs, Events, State>,
    private val eventActor: EventActor<Inputs, Events, State>,
    private val sideJobActor: SideJobActor<Inputs, Events, State>,
) : InputHandlerScope<Inputs, Events, State> {

    override val logger: BallastLogger get() = impl.logger

    override suspend fun getCurrentState(): State {
        guardian.checkStateAccess()
        return stateActor.getCurrentState()
    }

    override suspend fun updateState(block: (State) -> State) {
        guardian.checkStateUpdate()
        stateActor.safelyUpdateState(block)
    }

    override suspend fun updateStateAndGet(block: (State) -> State): State {
        guardian.checkStateUpdate()
        return stateActor.safelyUpdateStateAndGet(block)
    }

    override suspend fun getAndUpdateState(block: (State) -> State): State {
        guardian.checkStateUpdate()
        return stateActor.safelyGetAndUpdateState(block)
    }

    override suspend fun postEvent(event: Events) {
        guardian.checkPostEvent()
        eventActor.enqueueEvent(event, null, false)
    }

    override fun sideJob(
        key: String,
        block: suspend SideJobScope<Inputs, Events, State>.() -> Unit
    ) {
        guardian.checkSideJob()
        sideJobActor.enqueueSideJob(key, block)
    }

    override fun cancelSideJob(key: String) {
        guardian.checkSideJob()
        sideJobActor.cancelSideJob(key)
    }

    override fun noOp() {
        guardian.checkNoOp()
    }

    internal fun close() {
        guardian.close()
    }
}
