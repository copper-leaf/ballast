package com.copperleaf.ballast

import kotlinx.coroutines.CoroutineScope

/**
 * A scope to perform side-effects in. This scope is itself a coroutine scope, which is the same scope as that of the
 * originating Viewmodel, so if the ViewModel is cancelled, its side-effects will also be cancelled.
 *
 * Side Effects may post subsequent Inputs or Events during or after computation. Because side effects run in parallel
 * to the handling of events, in order to maintain good ordering of inputs and ensure state updates all correspond
 * directly to Inputs at the time they are processed, Side Effects cannot update the state directly. Instead, they must
 * post new Inputs back to the VM, to be processed at their proper point in time.
 */
@BallastDsl
public interface SideEffectScope<Inputs : Any, Events : Any, State : Any> : CoroutineScope {

    public enum class RestartState {
        Initial, Restarted
    }

    public val logger: BallastLogger
    public val currentStateWhenStarted: State
    public val restartState: RestartState

    /**
     * After performing a side-effect, dispatch a new Input back to the ViewModel (to update the State independently of
     * the current state with data computed during the side-effect).
     *
     * Inputs sent back to the ViewModel should not contain any data that can be derived from the ViewModel. That is,
     * data from the current State at the time this Side Effect finishes may not be the same as the sState of the
     * ViewModel at that point in time, since this Side Effect is running in parallel to the normal state updates. So
     * any Input sent back to the VM cannot assume any of the values in it's own "current state" are still valid, so we
     * definitely don't want to put any of those values back. Instead, let the Input pull the necessary values once it
     * is handled, instead of being cached in the Input class itself.
     *
     * An example would be saving data to a database to create a new record (a long-running task). After the record has
     * been inserted, we want to open up the editor tab for that new record, so we post an Input that focuses on the new
     * record.
     */
    public suspend fun postInput(input: Inputs)

    /**
     * During or after performing a side-effect, post an Event back to the ViewModel.
     *
     * An example would be saving data to a database, and then posting an Event to display a notification popup with
     * the result of the transaction.
     */
    public suspend fun postEvent(event: Events)
}
