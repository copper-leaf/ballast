package com.copperleaf.ballast.savedstate

import com.copperleaf.ballast.BallastInterceptorScope
import com.copperleaf.ballast.BallastLogger

public class RestoreStateScopeImpl<Inputs : Any, Events : Any, State : Any>(
    private val interceptorScope: BallastInterceptorScope<Inputs, Events, State>
) : RestoreStateScope<Inputs, Events, State> {

    override val logger: BallastLogger = interceptorScope.logger
    override val hostViewModelName: String = interceptorScope.hostViewModelName

    internal val inputToPostAfterRestore = mutableListOf<Inputs>()
    internal val eventsToPostAfterRestore = mutableListOf<Events>()

    override fun postInput(input: Inputs) {
        inputToPostAfterRestore += input
    }

    override fun postEvent(event: Events) {
        eventsToPostAfterRestore += event
    }
}
