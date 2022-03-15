package com.copperleaf.ballast.test.internal.vm

import com.copperleaf.ballast.BallastLogger
import com.copperleaf.ballast.EventHandlerScope
import kotlinx.coroutines.CompletableDeferred

internal class TestEventHandlerScope<Inputs : Any, Events : Any, State : Any>(
    private val eventHandlerScopeDelegate: EventHandlerScope<TestViewModel.Inputs<Inputs>, Events, State>
) : EventHandlerScope<Inputs, Events, State> {
    override val logger: BallastLogger get() = eventHandlerScopeDelegate.logger

    override suspend fun postInput(input: Inputs) {
        val deferred = CompletableDeferred<Unit>()
        eventHandlerScopeDelegate.postInput(
            TestViewModel.Inputs.ProcessInput(input, deferred)
        )
        deferred.await()
    }
}
