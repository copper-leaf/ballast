package com.copperleaf.ballast.savedstate

import com.copperleaf.ballast.BallastLogger
import com.copperleaf.ballast.EventHandler

public interface RestoreStateScope<Inputs : Any, Events : Any, State : Any> {

    public val logger: BallastLogger
    public val hostViewModelName: String

    /**
     * Post an Input back to the ViewModel's queue after the state has been fully restored. This Input will not be
     * dispatched to the queue until after [SavedStateAdapter.restore] has returned.
     *
     * Events posted with [RestoreStateScope.postEvent] will be dispatched before any Inputs sent from [postInput].
     */
    public fun postInput(input: Inputs)

    /**
     * Post an Event to the ViewModel's [EventHandler] after the state has been fully restored. This Event will not be
     * dispatched to the queue until after [SavedStateAdapter.restore] has returned.
     *
     * Events posted with [RestoreStateScope.postEvent] will be dispatched before any Inputs sent from [postInput].
     */
    public fun postEvent(event: Events)
}
