package com.copperleaf.ballast.internal.scopes

import com.copperleaf.ballast.BallastInterceptor
import com.copperleaf.ballast.BallastLogger
import com.copperleaf.ballast.EventHandlerScope
import com.copperleaf.ballast.Queued
import com.copperleaf.ballast.internal.actors.InputActor
import com.copperleaf.ballast.internal.actors.InterceptorActor

internal class EventHandlerScopeImpl<Inputs : Any, Events : Any, State : Any>(
    override val logger: BallastLogger,

    private val inputActor: InputActor<Inputs, Events, State>,
    private val interceptorActor: InterceptorActor<Inputs, Events, State>,
) : EventHandlerScope<Inputs, Events, State> {

    override suspend fun postInput(input: Inputs) {
        inputActor.enqueueQueued(Queued.HandleInput(null, input), await = false)
    }

    override suspend fun <I : BallastInterceptor<*, *, *>> getInterceptor(key: BallastInterceptor.Key<I>): I {
        return interceptorActor.getInterceptor(key)
    }

    fun ensureUsedCorrectly() {
        // no-op
    }
}
