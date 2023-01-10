package com.copperleaf.ballast.internal

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

    fun ensureUsedCorrectly() {
        // no-op
    }
}
