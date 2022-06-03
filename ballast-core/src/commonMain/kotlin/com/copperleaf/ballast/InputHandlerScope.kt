package com.copperleaf.ballast

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.updateAndGet

/**
 * A scope for handling the "blocking" processing of an Input that was accepted by the VM. This scope defines the
 * actions that can be taken "synchronously" during the processing on an Input, and checks to make sure that it is being
 * handled correctly.
 *
 * The processing of this [InputHandlerScope] is protected by an [InputStrategy.Guardian] to ensure that it is being
 * used correctly according to the rules of the [InputStrategy] provided to the [BallastViewModelConfiguration]
 * ([BallastViewModelConfiguration.inputStrategy]).
 */
@BallastDsl
public interface InputHandlerScope<Inputs : Any, Events : Any, State : Any> {

    /**
     * A reference to the [BallastLogger] set in the host ViewModel's [BallastViewModelConfiguration]
     * ([BallastViewModelConfiguration.logger]).
     */
    public val logger: BallastLogger

    /**
     * Returns the current state at the moment this function is called.
     *
     * @see [MutableStateFlow.value]
     */
    public suspend fun getCurrentState(): State

    /**
     * Atomically update the VM state given the current state.
     *
     * @See [MutableStateFlow.update]
     */
    public suspend fun updateState(block: (State) -> State)

    /**
     * Atomically update the VM state given the current state, and return the new state.
     *
     * @See [MutableStateFlow.updateAndGet]
     */
    public suspend fun updateStateAndGet(block: (State) -> State): State

    /**
     * Atomically update the VM state given the current state, and return the previous state.
     *
     * @See [MutableStateFlow.getAndUpdate]
     */
    public suspend fun getAndUpdateState(block: (State) -> State): State

    /**
     * Post an event which will eventually be dispatched to the VM's event handler. This event should be derived
     * entirely from the given Input, with no data from the State. If the event needs anything from the State, use
     * [postEventWithState] instead.
     */
    public suspend fun postEvent(event: Events)

    /**
     * Do something other than update the VM state or dispatch an Event. This is moving outside the normal MVI workflow,
     * so make sure you know what you're doing with this, and try to make sure it can be undone. For example, when
     * deleting a record from the DB, do a soft delete so that it can be restored later, if needed.
     *
     * Side-jobs are not started until after the normal input-handling has completed. Side-jobs may safely be
     * restarted; already-running side-jobs at the same [key] will be cancelled when starting the new side-job. If
     * a single VM is starting multiple side-jobs (likely from different inputs), they should each be given a unique
     * [key] within the VM to ensure they do not accidentally cancel each other. A null key is treated no differently
     * than a key of any other string value.
     */
    public fun sideJob(
        key: String,
        block: suspend SideJobScope<Inputs, Events, State>.() -> Unit
    )

    /**
     * Explicitly mark a branch in the InputHandler as doing nothing, so that it is still considered
     * to have been handled properly even though nothing happened.
     */
    public fun noOp()
}
