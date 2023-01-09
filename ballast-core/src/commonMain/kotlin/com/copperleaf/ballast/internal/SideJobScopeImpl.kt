package com.copperleaf.ballast.internal

import com.copperleaf.ballast.BallastLogger
import com.copperleaf.ballast.SideJobScope
import kotlinx.coroutines.CoroutineScope
import kotlin.time.Duration

internal class SideJobScopeImpl<Inputs : Any, Events : Any, State : Any>(
    sideJobCoroutineScope: CoroutineScope,
    private val impl: BallastViewModelImpl<Inputs, Events, State>,
    override val key: String,
    override val currentStateWhenStarted: State,
    override val restartState: SideJobScope.RestartState,
) : SideJobScope<Inputs, Events, State>, CoroutineScope by sideJobCoroutineScope {

    override val logger: BallastLogger get() = impl.logger

    override suspend fun postInput(input: Inputs) {
        impl.enqueueInput(input, null, false)
    }

    override suspend fun postEvent(event: Events) {
        impl.enqueueEvent(event, null, false)
    }

    override suspend fun closeGracefully(gracePeriod: Duration) {
        impl.gracefullyShutDown(gracePeriod, null)
    }
}
