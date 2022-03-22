package com.copperleaf.ballast.internal

import com.copperleaf.ballast.BallastLogger
import com.copperleaf.ballast.SideJobScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.SendChannel

internal class SideJobScopeImpl<Inputs : Any, Events : Any, State : Any>(
    override val logger: BallastLogger,
    private val _inputs: SendChannel<Inputs>,
    private val _events: SendChannel<Events>,
    override val currentStateWhenStarted: State,
    override val restartState: SideJobScope.RestartState,
    private val coroutineScope: CoroutineScope,
) : SideJobScope<Inputs, Events, State>, CoroutineScope by coroutineScope {
    override suspend fun postInput(input: Inputs) {
        _inputs.send(input)
    }

    override suspend fun postEvent(event: Events) {
        _events.send(event)
    }
}
