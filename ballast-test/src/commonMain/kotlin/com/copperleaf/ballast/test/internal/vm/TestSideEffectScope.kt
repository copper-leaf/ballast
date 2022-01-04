package com.copperleaf.ballast.test.internal.vm

import com.copperleaf.ballast.SideEffectScope
import kotlinx.coroutines.CompletableDeferred

internal class TestSideEffectScope<Inputs : Any, Events : Any, State : Any>(
    private val sideEffectScopeDelegate: SideEffectScope<TestViewModel.Inputs<Inputs, State>, Events, State>
) : SideEffectScope<Inputs, Events, State> {

    override suspend fun postInput(input: Inputs) {
        val deferred = CompletableDeferred<Unit>()
        sideEffectScopeDelegate.postInput(
            TestViewModel.Inputs.ProcessInput(input, deferred)
        )
        deferred.await()
    }

    override suspend fun postEvent(event: Events) {
        sideEffectScopeDelegate.postEvent(event)
    }
}
