package com.copperleaf.ballast.internal

import com.copperleaf.ballast.BallastInterceptorScope
import com.copperleaf.ballast.BallastLogger
import com.copperleaf.ballast.Queued
import kotlinx.coroutines.CoroutineScope

internal class BallastInterceptorScopeImpl<Inputs : Any, Events : Any, State : Any>(
    interceptorCoroutineScope: CoroutineScope,
    private val impl: BallastViewModelImpl<Inputs, Events, State>,
) : BallastInterceptorScope<Inputs, Events, State>,
    CoroutineScope by interceptorCoroutineScope {

    override val logger: BallastLogger get() = impl.logger
    override val hostViewModelType: String get() = impl.type
    override val hostViewModelName: String get() = impl.name

    override suspend fun sendToQueue(queued: Queued<Inputs, Events, State>) {
        impl.enqueueQueued(queued)
    }

    override suspend fun postEvent(event: Events) {
        impl.enqueueEvent(event, null, false)
    }
}
