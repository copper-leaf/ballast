package com.copperleaf.ballast.internal

import com.copperleaf.ballast.BallastLogger
import com.copperleaf.ballast.EventHandlerScope

internal class EventHandlerScopeImpl<Inputs : Any, Events : Any, State : Any>(
    override val logger: BallastLogger,
    private val sendInputToViewModel: suspend (Inputs)->Unit,
) : EventHandlerScope<Inputs, Events, State> {
    override suspend fun postInput(input: Inputs) {
        sendInputToViewModel(input)
    }

    fun ensureUsedCorrectly() {
        // no-op
    }
}
