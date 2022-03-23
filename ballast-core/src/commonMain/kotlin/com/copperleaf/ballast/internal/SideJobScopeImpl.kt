package com.copperleaf.ballast.internal

import com.copperleaf.ballast.BallastLogger
import com.copperleaf.ballast.SideJobScope
import kotlinx.coroutines.CoroutineScope

internal class SideJobScopeImpl<Inputs : Any, Events : Any, State : Any>(
    override val logger: BallastLogger,
    private val sendInputToViewModel: suspend (Inputs)->Unit,
    private val sendEventToViewModel: suspend (Events)->Unit,
    override val currentStateWhenStarted: State,
    override val restartState: SideJobScope.RestartState,
    private val coroutineScope: CoroutineScope,
) : SideJobScope<Inputs, Events, State>, CoroutineScope by coroutineScope {
    override suspend fun postInput(input: Inputs) {
        sendInputToViewModel(input)
    }

    override suspend fun postEvent(event: Events) {
        sendEventToViewModel(event)
    }
}
