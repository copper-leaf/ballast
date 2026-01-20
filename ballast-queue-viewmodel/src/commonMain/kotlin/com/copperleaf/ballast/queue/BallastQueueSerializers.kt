package com.copperleaf.ballast.queue

import com.copperleaf.ballast.BallastDecoder
import com.copperleaf.ballast.BallastEncoder

internal class BallastQueueSerializers<Inputs : Any, Events : Any, State : Any>(
    val encoder: BallastEncoder<Inputs, Events, State>,
    val decoder: BallastDecoder<Inputs, Events, State>,
) : QueueExecutor.Serializers<Inputs, Events, State> {

    override fun serializePayload(payload: Inputs): String {
        return encoder.encodeInputToString(payload)
    }

    override fun deserializePayload(serializedPayload: String): Inputs {
        return decoder.decodeInputFromString(serializedPayload)
    }

    override fun serializeResult(result: Events): String {
        return encoder.encodeEventToString(result)
    }

    override fun deserializeResult(serializedResult: String): Events {
        return decoder.decodeEventFromString(serializedResult)
    }

    override fun serializeState(state: State): String {
        return encoder.encodeStateToString(state)
    }

    override fun deserializeState(serializedState: String): State {
        return decoder.decodeStateFromString(serializedState)
    }
}
