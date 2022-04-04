package com.copperleaf.ballast.savedstate

import com.copperleaf.ballast.BallastLogger

public class SaveStateScopeImpl<Inputs : Any, Events : Any, State : Any>(
    override val logger: BallastLogger,
    override val hostViewModelName: String,
    private val previousState: State?,
    private val nextState: State,
) : SaveStateScope<Inputs, Events, State> {

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
