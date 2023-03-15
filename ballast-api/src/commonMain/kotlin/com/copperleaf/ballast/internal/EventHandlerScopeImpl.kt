package com.copperleaf.ballast.internal

import com.copperleaf.ballast.BallastInterceptor
import com.copperleaf.ballast.BallastLogger
import com.copperleaf.ballast.EventHandlerScope
import com.copperleaf.ballast.Queued

internal class EventHandlerScopeImpl<Inputs : Any, Events : Any, State : Any>(
    private val impl: BallastViewModelImpl<Inputs, Events, State>,
) : EventHandlerScope<Inputs, Events, State> {

    override val logger: BallastLogger get() = impl.logger

    override suspend fun postInput(input: Inputs) {
        impl.enqueueQueued(Queued.HandleInput(null, input), await = false)
    }

    override suspend fun <I : BallastInterceptor<*, *, *>> getInterceptor(key: BallastInterceptor.Key<I>): I {
        return impl.getInterceptor(key)
    }

    fun ensureUsedCorrectly() {
        // no-op
    }
}
