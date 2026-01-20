package com.copperleaf.ballast.queue.scope

import com.copperleaf.ballast.BallastLogger
import com.copperleaf.ballast.BallastNotification
import com.copperleaf.ballast.SideJobScope
import com.copperleaf.ballast.internal.BallastViewModelImpl
import com.copperleaf.ballast.internal.scopes.InternalInputHandlerScope
import com.copperleaf.ballast.queue.JobQueueGuardian

internal class JobQueueInputHandlerScope<Inputs : Any, Events : Any, State : Any>(
    private val guardian: JobQueueGuardian<Events, State>,
    private val impl: BallastViewModelImpl<Inputs, Events, State>,
) : InternalInputHandlerScope<Inputs, Events, State> {
    override val logger: BallastLogger get() = impl.logger

    override suspend fun getCurrentState(): State {
        guardian.checkStateAccess()
        return guardian.queueExecutorScope.getCurrentState()
    }

    override suspend fun updateState(block: (State) -> State) {
        guardian.checkStateUpdate()
        val previousState = guardian.queueExecutorScope.getCurrentState()
        val updatedState = block(previousState)
        guardian.queueExecutorScope.setState(updatedState)

        // notify interceptors of state change. Mostly for logging purposes
        impl.interceptorActor.notify(BallastNotification.StateChanged(impl.type, impl.name, getCurrentState()))
    }

    override suspend fun updateStateAndGet(block: (State) -> State): State {
        guardian.checkStateUpdate()
        val previousState = guardian.queueExecutorScope.getCurrentState()
        val updatedState = block(previousState)
        guardian.queueExecutorScope.setState(updatedState)

        // notify interceptors of state change. Mostly for logging purposes
        impl.interceptorActor.notify(BallastNotification.StateChanged(impl.type, impl.name, getCurrentState()))

        return updatedState
    }

    override suspend fun getAndUpdateState(block: (State) -> State): State {
        guardian.checkStateUpdate()
        val previousState = guardian.queueExecutorScope.getCurrentState()
        val updatedState = block(previousState)
        guardian.queueExecutorScope.setState(updatedState)

        // notify interceptors of state change. Mostly for logging purposes
        impl.interceptorActor.notify(BallastNotification.StateChanged(impl.type, impl.name, getCurrentState()))

        return previousState
    }

    override suspend fun postEvent(event: Events) {
        guardian.checkPostEvent()
        guardian.setEventAsResult(event)

        // notify interceptors of state being emitted. Mostly for logging purposes
        impl.interceptorActor.notify(BallastNotification.EventEmitted(impl.type, impl.name, event))
    }

    override fun sideJob(
        key: String,
        block: suspend SideJobScope<Inputs, Events, State>.() -> Unit
    ) {
        guardian.checkSideJob()
        impl.sideJobActor.enqueueSideJob(key, block)
    }

    override fun cancelSideJob(key: String) {
        guardian.checkSideJob()
        impl.sideJobActor.cancelSideJob(key)
    }

    override fun noOp() {
        guardian.checkNoOp()
    }

    override fun markAsCompletedSuccessfully() {
        guardian.close()
    }
}
