package com.copperleaf.ballast.savedstate

import com.copperleaf.ballast.BallastInterceptorScope
import com.copperleaf.ballast.BallastLogger

internal class SaveStateScopeImpl<Inputs : Any, Events : Any, State : Any>(
    private val interceptorScope: BallastInterceptorScope<Inputs, Events, State>,
    private val previousState: State?,
    private val nextState: State,
) : SaveStateScope<Inputs, Events, State> {

    override val logger: BallastLogger = interceptorScope.logger
    override val hostViewModelName: String = interceptorScope.hostViewModelName

    override suspend fun <Prop> saveDiff(
        computeProperty: State.() -> Prop ,
        onChanged: suspend (Prop) -> Unit,
    ) {
        val previousValue = previousState?.computeProperty()
        val nextValue = nextState.computeProperty()

        if (previousValue == null || previousValue != nextValue) {
            onChanged(nextValue)
        }
    }

    override suspend fun saveAll(
        onChanged: suspend (State) -> Unit,
    ) {
        val previousValue = previousState
        val nextValue = nextState

        if (previousValue == null || previousValue != nextValue) {
            onChanged(nextValue)
        }
    }
}
