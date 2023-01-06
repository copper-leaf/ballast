package com.copperleaf.ballast.test.internal.vm

import com.copperleaf.ballast.BallastLogger
import com.copperleaf.ballast.SideJobScope
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlin.time.Duration

internal class TestSideJobScope<Inputs : Any, Events : Any, State : Any>(
    private val sideJobScopeDelegate: SideJobScope<TestViewModel.Inputs<Inputs>, Events, State>,
) : SideJobScope<Inputs, Events, State>, CoroutineScope by sideJobScopeDelegate {

    override val logger: BallastLogger get() = sideJobScopeDelegate.logger
    override val currentStateWhenStarted: State get() = sideJobScopeDelegate.currentStateWhenStarted
    override val restartState: SideJobScope.RestartState get() = sideJobScopeDelegate.restartState

    override suspend fun postInput(input: Inputs) {
        val deferred = CompletableDeferred<Unit>()
        sideJobScopeDelegate.postInput(
            TestViewModel.Inputs.ProcessInput(input, deferred)
        )
        deferred.await()
    }

    override suspend fun postEvent(event: Events) {
        sideJobScopeDelegate.postEvent(event)
    }

    override suspend fun closeGracefully(gracePeriod: Duration) {
        sideJobScopeDelegate.closeGracefully(gracePeriod)
    }
}
