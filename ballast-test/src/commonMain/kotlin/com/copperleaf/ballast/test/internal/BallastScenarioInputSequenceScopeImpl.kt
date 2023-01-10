package com.copperleaf.ballast.test.internal

import com.copperleaf.ballast.BallastInterceptorScope
import com.copperleaf.ballast.Queued
import com.copperleaf.ballast.test.BallastScenarioInputSequenceScope
import kotlinx.coroutines.CompletableDeferred

internal class BallastScenarioInputSequenceScopeImpl<Inputs : Any, Events : Any, State : Any>(
    private val interceptorScope: BallastInterceptorScope<Inputs, Events, State>
) : BallastScenarioInputSequenceScope<Inputs, Events, State> {

    override suspend fun send(input: Inputs) {
        interceptorScope.sendToQueue(Queued.HandleInput(null, input))
    }

    override suspend fun sendAndAwait(input: Inputs) {
        val deferred = CompletableDeferred<Unit>()
        interceptorScope.sendToQueue(Queued.HandleInput(deferred, input))
        deferred.await()
    }
}
