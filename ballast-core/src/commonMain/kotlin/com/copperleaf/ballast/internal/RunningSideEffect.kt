package com.copperleaf.ballast.internal

import com.copperleaf.ballast.SideEffectScope

internal class RunningSideEffect<Inputs : Any, Events : Any, State : Any>(
    val sideEffect: RestartableSideEffect<Inputs, Events, State>,
    val scope: SideEffectScope<Inputs, Events, State>,
) {
    fun start(latestState: State) {
        sideEffect.block(scope, latestState)
    }
}
