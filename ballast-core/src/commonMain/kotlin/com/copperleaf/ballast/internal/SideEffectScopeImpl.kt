package com.copperleaf.ballast.internal

import com.copperleaf.ballast.SideEffectScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.SendChannel

internal class SideEffectScopeImpl<Inputs : Any, Events : Any, State : Any>(
    private val _inputs: SendChannel<Inputs>,
    private val _events: SendChannel<Events>,
    override val currentStateWhenStarted: State,
    override val restartState: SideEffectScope.RestartState,
    private val coroutineScope: CoroutineScope,
) : SideEffectScope<Inputs, Events, State>, CoroutineScope by coroutineScope {
    override suspend fun postInput(input: Inputs) {
        _inputs.send(input)
    }

    override suspend fun postEvent(event: Events) {
        _events.send(event)
    }
}
