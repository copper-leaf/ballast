package com.copperleaf.ballast.debugger

import io.ktor.http.ContentType

internal class LambdaDebuggerAdapter<Inputs : Any, Events : Any, State : Any>(
    private val serializeInput: ((Inputs) -> Pair<ContentType, String>)?,
    private val serializeEvent: ((Events) -> Pair<ContentType, String>)?,
    private val serializeState: ((State) -> Pair<ContentType, String>)?,
) : DebuggerAdapter<Inputs, Events, State> {
    override fun serializeInput(input: Inputs): Pair<ContentType, String> {
        return serializeInput?.invoke(input)
            ?: (ContentType.Text.Any to input.toString())
    }

    override fun serializeEvent(event: Events): Pair<ContentType, String> {
        return serializeEvent?.invoke(event)
            ?: (ContentType.Text.Any to event.toString())
    }

    override fun serializeState(state: State): Pair<ContentType, String> {
        return serializeState?.invoke(state)
            ?: (ContentType.Text.Any to state.toString())
    }

    override fun deserializeInput(contentType: ContentType, serializedInput: String): Inputs? {
        return null
    }

    override fun deserializeState(contentType: ContentType, serializedState: String): State? {
        return null
    }

    override fun toString(): String {
        return "LambdaDebuggerAdapter"
    }
}
