package com.copperleaf.ballast.savedstate

import com.copperleaf.ballast.BallastLogger

public interface SaveStateScope<Inputs : Any, Events : Any, State : Any> {

    public val logger: BallastLogger
    public val hostViewModelName: String

    public suspend fun <Prop> saveDiff(
        computeProperty: State.() -> Prop,
        onChanged: suspend (Prop) -> Unit,
    )

    public suspend fun saveAll(
        onChanged: suspend (State) -> Unit,
    )
}
