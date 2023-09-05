package com.copperleaf.ballast.debugger

import io.ktor.http.ContentType

public interface DebuggerAdapter<Inputs : Any, Events : Any, State : Any> {
    public fun serializeInput(input: Inputs): Pair<ContentType, String> {
        return ContentType.Text.Any to input.toString()
    }

    public fun serializeEvent(event: Events): Pair<ContentType, String> {
        return ContentType.Text.Any to event.toString()
    }

    public fun serializeState(state: State): Pair<ContentType, String> {
        return ContentType.Text.Any to state.toString()
    }

    public fun deserializeState(contentType: ContentType, serializedState: String): State? {
        return null
    }

    public fun deserializeInput(contentType: ContentType, serializedInput: String): Inputs? {
        return null
    }
}
