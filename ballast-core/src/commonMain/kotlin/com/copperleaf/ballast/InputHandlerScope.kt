package com.copperleaf.ballast

import kotlinx.coroutines.flow.MutableStateFlow

/**
 * A scope for handling the "blocking" processing of an Input that was accepted by the VM. Inputs
 * may update the VM state, post events, and start side-effects. Side-effects are idempotent in that
 * if a side-effect at the same key is started, the previous one is cancelled to avoid leaks.
 *
 * Side-effects are not started until the normal processing has completed. They are given more
 * freedom to launch coroutines that run in the background, parallel to normal Input handling, and
 * may post new Inputs or Events. However, the side-effect cannot update the state, as that would
 * lead to race conditions. When a side-effect would need to update the state, it should post an
 * Input which applies that value to the State.
 *
 * Posting Inputs directly from the "blocking" scope may cause deadlocks, as as such must be posted
 * from a side-effect so that it is queued up as normal. In addition, posting an Input from an Input
 * doesn't make sense, as you can simply extract the "handler" code from that Input and execute it
 * directly, without going through the intermediary step of posting an extra Input.
 */
@BallastDsl
public interface InputHandlerScope<Inputs : Any, Events : Any, State : Any> {

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
     * Side-effects are not started until after the normal input-handling has completed. Side-effects may safely be
     * restarted; already-running side-effects at the same [key] will be cancelled when starting the new side-effect. If
     * a single VM is starting multiple side-effects (likely from different inputs), they should each be given a unique
     * [key] within the VM to ensure they do not accidentally cancel each other. A null key is treated no differently
     * than a key of any other string value.
     */
    public fun sideEffect(
        key: String,
        block: suspend SideEffectScope<Inputs, Events, State>.() -> Unit
    )

    /**
     * Explicitly mark a branch in the InputHandler as doing nothing, so that it is still considered
     * to have been handled properly even though nothing happened.
     */
    public fun noOp()
}
