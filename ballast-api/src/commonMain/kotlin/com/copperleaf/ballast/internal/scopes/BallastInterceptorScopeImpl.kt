package com.copperleaf.ballast.internal.scopes

import com.copperleaf.ballast.BallastInterceptorScope
import com.copperleaf.ballast.BallastLogger
import com.copperleaf.ballast.Queued
import com.copperleaf.ballast.internal.BallastViewModelImpl
import com.copperleaf.ballast.internal.actors.EventActor
import com.copperleaf.ballast.internal.actors.InputActor
import kotlinx.coroutines.CoroutineScope

internal class BallastInterceptorScopeImpl<Inputs : Any, Events : Any, State : Any>(
    interceptorCoroutineScope: CoroutineScope,
    private val impl: BallastViewModelImpl<Inputs, Events, State>,

    private val inputActor: InputActor<Inputs, Events, State>,
    private val eventActor: EventActor<Inputs, Events, State>,
) : BallastInterceptorScope<Inputs, Events, State>,
    CoroutineScope by interceptorCoroutineScope {

    override val logger: BallastLogger get() = impl.logger
    override val hostViewModelType: String get() = impl.type
    override val hostViewModelName: String get() = impl.name

    override suspend fun sendToQueue(queued: Queued<Inputs, Events, State>) {
        inputActor.enqueueQueued(queued, await = false)
    }

    override suspend fun postEvent(event: Events) {
        eventActor.enqueueEvent(event, null, false)
    }
}
