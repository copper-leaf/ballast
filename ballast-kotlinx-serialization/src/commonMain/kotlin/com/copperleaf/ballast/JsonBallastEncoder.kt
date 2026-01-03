package com.copperleaf.ballast

import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json

public class JsonBallastEncoder<Inputs : Any, Events : Any, State : Any>(
    private val inputsSerializer: KSerializer<Inputs>,
    private val eventsSerializer: KSerializer<Events>,
    private val stateSerializer: KSerializer<State>,
    private val json: Json = Json { prettyPrint = true },
) : BallastEncoder<Inputs, Events, State>, BallastDecoder<Inputs, Events, State> {

    override val contentType: String = "application/json"

    override fun encodeInputToString(input: Inputs): String {
        return json.encodeToString(inputsSerializer, input)
    }

    override fun encodeEventToString(event: Events): String {
        return json.encodeToString(eventsSerializer, event)
    }

    override fun encodeStateToString(state: State): String {
        return json.encodeToString(stateSerializer, state)
    }

    override fun decodeInputFromString(encoded: String): Inputs {
        return json.decodeFromString(inputsSerializer, encoded)
    }

    override fun decodeEventFromString(encoded: String): Events {
        return json.decodeFromString(eventsSerializer, encoded)
    }

    override fun decodeStateFromString(encoded: String): State {
        return json.decodeFromString(stateSerializer, encoded)
    }
}
