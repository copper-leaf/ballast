package com.copperleaf.ballast.internal

import com.copperleaf.ballast.EventHandlerScope
import kotlinx.coroutines.channels.SendChannel

internal class EventHandlerScopeImpl<Inputs : Any, Events : Any, State : Any>(
    private val _inputs: SendChannel<Inputs>,
) : EventHandlerScope<Inputs, Events, State> {
    override suspend fun postInput(input: Inputs) {
        _inputs.send(input)
    }

    fun ensureUsedCorrectly() {
        // no-op
    }
}
