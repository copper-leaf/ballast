package com.copperleaf.ballast.undo

import com.copperleaf.ballast.BallastInterceptorScope
import com.copperleaf.ballast.Queued
import kotlinx.coroutines.CoroutineScope

public class UndoScopeImpl<Inputs : Any, Events : Any, State : Any>(
    private val interceptorScope: BallastInterceptorScope<Inputs, Events, State>
) : UndoScope<Inputs, Events, State>, CoroutineScope by interceptorScope {
    public override suspend fun restoreState(state: State) {
        interceptorScope.sendToQueue(Queued.RestoreState(null, state))
    }
}
