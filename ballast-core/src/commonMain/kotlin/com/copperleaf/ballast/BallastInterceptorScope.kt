package com.copperleaf.ballast

import kotlinx.coroutines.CoroutineScope

public interface BallastInterceptorScope<Inputs : Any, Events : Any, State : Any> : CoroutineScope {

    public val logger: BallastLogger
    public val hostViewModelName: String

    public suspend fun sendToQueue(queued: Queued<Inputs, Events, State>)

}
