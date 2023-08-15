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
        computeProperty: State.() -> Prop,
        onChanged: suspend (Prop) -> Unit,
    ) {
        saveDiff(
            computeProperty = computeProperty,
            isChanged = { previousValue, nextValue -> previousValue != nextValue },
            onChanged = onChanged,
        )
    }

    override suspend fun <Prop> saveDiff(
        computeProperty: State.() -> Prop,
        isChanged: (Prop, Prop) -> Boolean,
        onChanged: suspend (Prop) -> Unit
    ) {
        val previousValue = previousState?.computeProperty()
        val nextValue = nextState.computeProperty()

        if (previousValue == null || isChanged(previousValue, nextValue)) {
            onChanged(nextValue)
        }
    }

    override suspend fun saveAll(
        onChanged: suspend (State) -> Unit,
    ) {
        saveAll(
            isChanged = { previousValue, nextValue -> previousValue != nextValue },
            onChanged = onChanged,
        )
    }

    override suspend fun saveAll(
        isChanged: (State, State) -> Boolean,
        onChanged: suspend (State) -> Unit,
    ) {
        val previousValue = previousState
        val nextValue = nextState

        if (previousValue == null || isChanged(previousValue, nextValue)) {
            onChanged(nextValue)
        }
    }
}
