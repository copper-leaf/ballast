package com.copperleaf.ballast.debugger

import io.ktor.http.ContentType

public class ToStringDebuggerAdapter<Inputs : Any, Events : Any, State : Any> :
    DebuggerAdapter<Inputs, Events, State> {

    override fun serializeInput(input: Inputs): Pair<ContentType, String> {
        return ContentType.Text.Any to input.toString()
    }

    override fun serializeEvent(event: Events): Pair<ContentType, String> {
        return ContentType.Text.Any to event.toString()
    }

    override fun serializeState(state: State): Pair<ContentType, String> {
        return ContentType.Text.Any to state.toString()
    }

    override fun deserializeInput(contentType: ContentType, serializedInput: String): Inputs? {
        return null
    }

    override fun deserializeState(contentType: ContentType, serializedState: String): State? {
        return null
    }

    override fun toString(): String {
        return "ToStringDebuggerAdapter"
    }
}
