package com.copperleaf.ballast

public interface BallastEncoder<Inputs : Any, Events : Any, State : Any> {

    public val contentType: String? get() = null

    public fun encodeInputToString(input: Inputs): String
    public fun encodeEventToString(event: Events): String
    public fun encodeStateToString(state: State): String
}
