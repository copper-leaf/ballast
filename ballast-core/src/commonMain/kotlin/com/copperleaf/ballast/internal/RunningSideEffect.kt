package com.copperleaf.ballast.internal

import com.copperleaf.ballast.SideEffectScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

internal class RunningSideEffect<Inputs : Any, Events : Any, State : Any>(
    internal val sideEffect: RestartableSideEffect<Inputs, Events, State>,
    internal val coroutineScope: CoroutineScope,
    internal val scope: SideEffectScope<Inputs, Events, State>,
) {
    internal var job: Job? = null
    fun start(latestState: State) {
        job = coroutineScope.launch {
            sideEffect.block(scope, latestState)
        }
    }
}
