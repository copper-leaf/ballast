package com.copperleaf.ballast.debugger

import io.ktor.http.ContentType
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json

public class JsonDebuggerAdapter<Inputs : Any, Events : Any, State : Any>(
    private val inputsSerializer: KSerializer<Inputs>? = null,
    private val eventsSerializer: KSerializer<Events>? = null,
    private val stateSerializer: KSerializer<State>? = null,
    private val json: Json = Json,
) : DebuggerAdapter<Inputs, Events, State> {
    override fun serializeInput(input: Inputs): Pair<ContentType, String> {
        return if (inputsSerializer != null) {
            ContentType.Application.Json to json.encodeToString(inputsSerializer, input)
        } else {
            ContentType.Text.Any to input.toString()
        }
    }

    override fun serializeEvent(event: Events): Pair<ContentType, String> {
        return if (eventsSerializer != null) {
            ContentType.Application.Json to json.encodeToString(eventsSerializer, event)
        } else {
            ContentType.Text.Any to event.toString()
        }
    }

    override fun serializeState(state: State): Pair<ContentType, String> {
        return if (stateSerializer != null) {
            ContentType.Application.Json to json.encodeToString(stateSerializer, state)
        } else {
            ContentType.Text.Any to state.toString()
        }
    }

    override fun deserializeInput(contentType: ContentType, serializedInput: String): Inputs? {
        return if (inputsSerializer != null) {
            check(contentType == ContentType.Application.Json)
            json.decodeFromString(inputsSerializer, serializedInput)
        } else {
            null
        }
    }

    override fun deserializeState(contentType: ContentType, serializedState: String): State? {
        return if (stateSerializer != null) {
            check(contentType == ContentType.Application.Json)
            json.decodeFromString(stateSerializer, serializedState)
        } else {
            null
        }
    }

    override fun toString(): String {
        return "JsonDebuggerAdapter"
    }
}
