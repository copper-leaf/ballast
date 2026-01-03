package com.copperleaf.ballast

public interface BallastDecoder<Inputs : Any, Events : Any, State : Any> {

    public fun decodeInputFromString(encoded: String): Inputs
    public fun decodeEventFromString(encoded: String): Events
    public fun decodeStateFromString(encoded: String): State
}

