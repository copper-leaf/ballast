package com.copperleaf.ballast

import kotlinx.coroutines.CoroutineScope

/**
 * A scope to perform side-jobs in. This scope is itself a coroutine scope, which is the same scope as that of the
 * originating Viewmodel, so if the ViewModel is cancelled, its side-jobs will also be cancelled.
 *
 * Side-jobs may post subsequent Inputs or Events during or after computation. Because side-jobs run in parallel
 * to the handling of events, in order to maintain good ordering of inputs and ensure state updates all correspond
 * directly to Inputs at the time they are processed, side-jobs cannot update the state directly. Instead, they must
 * post new Inputs back to the VM, to be processed at their proper point in time.
 */
@BallastDsl
public interface SideJobScope<Inputs : Any, Events : Any, State : Any> : CoroutineScope {

    /**
     * The key provided to the [InputHandlerScope.sideJob] call.
     */
    public val key: String

    /**
     * A reference to the [BallastLogger] set in the host ViewModel's [BallastViewModelConfiguration]
     * ([BallastViewModelConfiguration.logger]).
     */
    public val logger: BallastLogger

    /**
     * A snapshot of the ViewModel State at the point when this sideJob was started. There is no guarantee that this is
     * the same state as when [InputHandlerScope.sideJob] was called. If you need the state at that specific point in
     * time, you can capture a reference to the result of [InputHandlerScope.getCurrentState] in the sideJob's lambda.
     *
     * This property was deprecated in v3, to be removed in v4. Pass a snapshot of the state you need into the lambda to
     * avoid potential issues from the state not being what you expect it should be.
     */
    @Deprecated("Pass a snapshot of the state directly to the sideJob rather than using this property. Deprecated since v3, to be removed in v4.")
    public val currentStateWhenStarted: State

    /**
     * A flag to let you know whether this is the first time this sideJob was started at the given key, or if it was
     * called again to restart this task. A sideJob that completes normally and is then started again later at the same
     * key will be considered a new sideJob, so this will then be [RestartState.Initial]
     */
    public val restartState: RestartState

    /**
     * After start a side-job, dispatch a new Input back to the ViewModel (to update the State independently of
     * the current state with data computed during the side-job).
     *
     * Inputs sent back to the ViewModel should not contain any data that can be derived from the ViewModel. That is,
     * data from the current State at the time this side-job finishes may not be the same as the State of the
     * ViewModel at that point in time, since this side-job is running in parallel to the normal state updates. So
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
     * During or after performing a side-job, post an Event back to the ViewModel.
     *
     * An example would be saving data to a database, and then posting an Event to display a notification popup with
     * the result of the transaction.
     */
    public suspend fun postEvent(event: Events)

    public enum class RestartState {
        Initial, Restarted
    }
}
