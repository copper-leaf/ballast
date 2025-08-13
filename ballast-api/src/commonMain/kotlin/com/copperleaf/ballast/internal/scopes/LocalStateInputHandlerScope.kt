package com.copperleaf.ballast.internal.scopes

import com.copperleaf.ballast.BallastLogger
import com.copperleaf.ballast.LocalStateInputStrategy
import com.copperleaf.ballast.SideJobScope
import com.copperleaf.ballast.internal.actors.EventActor
import com.copperleaf.ballast.internal.actors.SideJobActor

internal class LocalStateInputHandlerScope<Inputs : Any, Events : Any, State : Any>(
    private val guardian: LocalStateInputStrategy.Guardian<State>,

    override val logger: BallastLogger,

    private val eventActor: EventActor<Inputs, Events, State>,
    private val sideJobActor: SideJobActor<Inputs, Events, State>,
) : InternalInputHandlerScope<Inputs, Events, State> {

    override suspend fun getCurrentState(): State {
        guardian.checkStateAccess()
        return guardian.getCurrentState()
    }

    override suspend fun updateState(block: (State) -> State) {
        guardian.checkStateUpdate()
        guardian.updateState(block)
    }

    override suspend fun updateStateAndGet(block: (State) -> State): State {
        guardian.checkStateUpdate()
        return guardian.updateStateAndGet(block)
    }

    override suspend fun getAndUpdateState(block: (State) -> State): State {
        guardian.checkStateUpdate()
        return guardian.getAndUpdateState(block)
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

    override fun markAsCompletedSuccessfully() {
        guardian.close()
    }
}
