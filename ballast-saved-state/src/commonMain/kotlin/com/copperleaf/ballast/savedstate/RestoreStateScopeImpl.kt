package com.copperleaf.ballast.savedstate

import com.copperleaf.ballast.BallastLogger

public class RestoreStateScopeImpl<Inputs : Any, Events : Any, State : Any>(
    override val logger: BallastLogger,
    override val hostViewModelName: String,
) : RestoreStateScope<Inputs, Events, State> {

    internal val inputToPostAfterRestore = mutableListOf<Inputs>()
    internal val eventsToPostAfterRestore = mutableListOf<Events>()

    override fun postInputAfterRestore(input: Inputs) {
        inputToPostAfterRestore += input
    }

    override fun postEventAfterRestore(event: Events) {
        eventsToPostAfterRestore += event
    }
}
