package com.copperleaf.ballast.savedstate

import com.copperleaf.ballast.BallastLogger

public interface SaveStateScope<Inputs : Any, Events : Any, State : Any> {

    public val logger: BallastLogger
    public val hostViewModelName: String

    /**
     * Save the value of [computeProperty] if it is not equal to the previous state's value. Equality is checked with
     * the [equals] operator (`!=`).
     */
    public suspend fun <Prop> saveDiff(
        computeProperty: State.() -> Prop,
        onChanged: suspend (Prop) -> Unit,
    )

    /**
     * Save the value of [computeProperty] if it is not equal to the previous state's value. Equality is checked with
     * a custom comparator function [isChanged], which should return true if the value is considered to be different.
     */
    public suspend fun <Prop> saveDiff(
        computeProperty: State.() -> Prop,
        isChanged: (Prop, Prop) -> Boolean,
        onChanged: suspend (Prop) -> Unit,
    )

    /**
     * Save the entire State object anytime it changes.
     */
    public suspend fun saveAll(
        onChanged: suspend (State) -> Unit,
    )

    public suspend fun saveAll(
        isChanged: (State, State) -> Boolean,
        onChanged: suspend (State) -> Unit,
    )
}
