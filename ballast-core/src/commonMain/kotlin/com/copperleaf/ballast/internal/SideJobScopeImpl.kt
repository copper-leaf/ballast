package com.copperleaf.ballast.internal

import com.copperleaf.ballast.BallastLogger
import com.copperleaf.ballast.Queued
import com.copperleaf.ballast.SideJobScope
import kotlinx.coroutines.CoroutineScope

internal class SideJobScopeImpl<Inputs : Any, Events : Any, State : Any>(
    sideJobCoroutineScope: CoroutineScope,
    private val impl: BallastViewModelImpl<Inputs, Events, State>,
    override val key: String,
    @Deprecated("Pass a snapshot of the state directly to the sideJob rather than using this property. Deprecated since v3, to be removed in v4.")
    override val currentStateWhenStarted: State,
    override val restartState: SideJobScope.RestartState,
) : SideJobScope<Inputs, Events, State>, CoroutineScope by sideJobCoroutineScope {

    override val logger: BallastLogger get() = impl.logger

    override suspend fun postInput(input: Inputs) {
        impl.enqueueQueued(Queued.HandleInput(null, input), await = false)
    }

    override suspend fun postEvent(event: Events) {
        impl.enqueueEvent(event, null, false)
    }
}
