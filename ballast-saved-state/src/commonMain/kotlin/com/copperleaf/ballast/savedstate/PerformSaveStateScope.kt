package com.copperleaf.ballast.savedstate

public interface PerformSaveStateScope<Inputs : Any, Events : Any, State : Any> {

    public val hostViewModelName: String

    public suspend fun <Prop> saveDiff(
        computeProperty: State.() -> Prop,
        onChanged: suspend (Prop) -> Unit,
    )

    public suspend fun saveAll(
        onChanged: suspend (State) -> Unit,
    )
}
