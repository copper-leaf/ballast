package com.copperleaf.ballast

import kotlinx.coroutines.CompletableDeferred

public sealed class Queued<Inputs : Any, Events : Any, State : Any> {

    public class RestoreState<Inputs : Any, Events : Any, State : Any>(
        public val state: State,
    ) : Queued<Inputs, Events, State>()

    public class HandleInput<Inputs : Any, Events : Any, State : Any>(
        public val deferred: CompletableDeferred<Unit>?,
        public val input: Inputs,
    ) : Queued<Inputs, Events, State>()
}
