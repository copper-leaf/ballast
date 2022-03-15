package com.copperleaf.ballast.internal

import com.copperleaf.ballast.BallastInterceptorScope
import com.copperleaf.ballast.BallastLogger
import com.copperleaf.ballast.Queued
import kotlinx.coroutines.CoroutineScope

public class BallastInterceptorScopeImpl<Inputs : Any, Events : Any, State : Any>(
    override val logger: BallastLogger,
    override val hostViewModelName: String,
    private val viewModelScope: CoroutineScope,
    private val sendQueuedToViewModel: suspend (Queued<Inputs, Events, State>) -> Unit
) : BallastInterceptorScope<Inputs, Events, State>,
    CoroutineScope by viewModelScope {

    override suspend fun sendToQueue(queued: Queued<Inputs, Events, State>) {
        sendQueuedToViewModel(queued)
    }

}
