package com.copperleaf.ballast

public class KSerializerEncoder<Inputs : Any, Events : Any, State : Any>() : BallastEncoder<Inputs, Events, State>, BallastDecoder<Inputs, Events, State> {

    override fun encodeInputToString(input: Inputs): String {
        TODO("Not yet implemented")
    }

    override fun encodeEventToString(event: Events): String {
        TODO("Not yet implemented")
    }

    override fun encodeStateToString(state: State): String {
        TODO("Not yet implemented")
    }

    override fun decodeInputFromString(encoded: String): Inputs {
        TODO("Not yet implemented")
    }

    override fun decodeEventFromString(encoded: String): Events {
        TODO("Not yet implemented")
    }

    override fun decodeStateFromString(encoded: String): State {
        TODO("Not yet implemented")
    }
}
