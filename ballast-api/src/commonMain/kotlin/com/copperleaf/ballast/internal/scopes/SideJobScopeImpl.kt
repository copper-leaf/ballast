package com.copperleaf.ballast.internal.scopes

import com.copperleaf.ballast.BallastInterceptor
import com.copperleaf.ballast.BallastLogger
import com.copperleaf.ballast.Queued
import com.copperleaf.ballast.SideJobScope
import com.copperleaf.ballast.internal.BallastViewModelImpl
import com.copperleaf.ballast.internal.actors.EventActor
import com.copperleaf.ballast.internal.actors.InputActor
import com.copperleaf.ballast.internal.actors.InterceptorActor
import kotlinx.coroutines.CoroutineScope

internal class SideJobScopeImpl<Inputs : Any, Events : Any, State : Any>(
    sideJobCoroutineScope: CoroutineScope,
    private val impl: BallastViewModelImpl<Inputs, Events, State>,

    private val inputActor: InputActor<Inputs, Events, State>,
    private val eventActor: EventActor<Inputs, Events, State>,
    private val interceptorActor: InterceptorActor<Inputs, Events, State>,

    override val key: String,
    override val restartState: SideJobScope.RestartState,
) : SideJobScope<Inputs, Events, State>, CoroutineScope by sideJobCoroutineScope {

    override val logger: BallastLogger get() = impl.logger

    override suspend fun postInput(input: Inputs) {
        inputActor.enqueueQueued(Queued.HandleInput(null, input), await = false)
    }

    override suspend fun postEvent(event: Events) {
        eventActor.enqueueEvent(event, null, false)
    }

    override suspend fun <I : BallastInterceptor<*, *, *>> getInterceptor(key: BallastInterceptor.Key<I>): I {
        return interceptorActor.getInterceptor(key)
    }
}
