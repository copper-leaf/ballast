package com.copperleaf.ballast

import kotlinx.coroutines.CompletableDeferred

/**
 * Represents an item in the ViewModel's main processing queue. Queued items will always be processed predicably
 * according to the rules of the [InputStrategy] provided to the [BallastViewModelConfiguration]
 * ([BallastViewModelConfiguration.inputStrategy]).
 */
public sealed class Queued<Inputs : Any, Events : Any, State : Any> {

    /**
     * A request to forcibly set the State to a specific value.
     */
    public class RestoreState<Inputs : Any, Events : Any, State : Any>(
        public val deferred: CompletableDeferred<Unit>?,
        public val state: State,
    ) : Queued<Inputs, Events, State>()

    /**
     * A request to handle an Input by sending it to the [InputHandler].
     */
    public class HandleInput<Inputs : Any, Events : Any, State : Any>(
        public val deferred: CompletableDeferred<Unit>?,
        public val input: Inputs,
    ) : Queued<Inputs, Events, State>()
}
