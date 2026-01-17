package com.copperleaf.ballast.queue.executor

import com.copperleaf.ballast.queue.QueueExecutor
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json

public class JsonSerializers<
        Payload : Any,
        Result : Any,
        State : Any,
        >(
    private val payloadSerializer: KSerializer<Payload>,
    private val resultSerializer: KSerializer<Result>,
    private val stateSerializer: KSerializer<State>,
    private val json: Json = Json.Default,
) : QueueExecutor.Serializers<Payload, Result, State> {
    override fun serializePayload(payload: Payload): String {
        return json.encodeToString(payloadSerializer, payload)
    }

    override fun deserializePayload(serializedPayload: String): Payload {
        return json.decodeFromString(payloadSerializer, serializedPayload)
    }

    override fun serializeResult(result: Result): String {
        return json.encodeToString(resultSerializer, result)
    }

    override fun deserializeResult(serializedResult: String): Result {
        return json.decodeFromString(resultSerializer, serializedResult)
    }

    override fun serializeState(state: State): String {
        return json.encodeToString(stateSerializer, state)
    }

    override fun deserializeState(serializedState: String): State {
        return json.decodeFromString(stateSerializer, serializedState)
    }
}
