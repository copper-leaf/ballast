package com.copperleaf.ballast.debugger

import com.copperleaf.ballast.BallastInterceptor
import com.copperleaf.ballast.BallastInterceptorScope
import com.copperleaf.ballast.BallastNotification
import io.ktor.http.ContentType
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json

public class BallastDebuggerInterceptor<Inputs : Any, Events : Any, State : Any>(
    private val connection: BallastDebuggerClientConnection<*>,
    private val serializeInput: (Inputs) -> Pair<ContentType, String> = { ContentType.Text.Any to it.toString() },
    private val serializeEvent: (Events) -> Pair<ContentType, String> = { ContentType.Text.Any to it.toString() },
    private val serializeState: (State) -> Pair<ContentType, String> = { ContentType.Text.Any to it.toString() },
    private val deserializeState: ((ContentType, String) -> State)? = null,
    private val deserializeInput: ((ContentType, String) -> Inputs)? = null,
) : BallastInterceptor<Inputs, Events, State> {

    override fun BallastInterceptorScope<Inputs, Events, State>.start(notifications: Flow<BallastNotification<Inputs, Events, State>>) {
        with(connection) {
            connectViewModel(
                BallastDebuggerViewModelConnection(
                    notifications = notifications,
                    viewModelName = hostViewModelName,
                    serializeInput = serializeInput,
                    serializeEvent = serializeEvent,
                    serializeState = serializeState,
                    deserializeState = deserializeState,
                    deserializeInput = deserializeInput,
                )
            )
        }
    }

    override fun toString(): String {
        return "BallastDebuggerInterceptor"
    }

    public companion object {
        public fun <Inputs : Any, Events : Any, State : Any> withJson(
            connection: BallastDebuggerClientConnection<*>,
            stateSerializer: KSerializer<State>? = null,
            inputsSerializer: KSerializer<Inputs>? = null,
            eventsSerializer: KSerializer<Events>? = null,
        ): BallastDebuggerInterceptor<Inputs, Events, State> {
            val json = ContentType.Application.Json
            val plainText = ContentType.Text.Any
            return BallastDebuggerInterceptor(
                connection,
                serializeInput = if (inputsSerializer != null) {
                    { input -> json to Json.encodeToString(inputsSerializer, input) }
                } else {
                    { input -> plainText to input.toString() }
                },
                serializeEvent = if (eventsSerializer != null) {
                    { event -> json to Json.encodeToString(eventsSerializer, event) }
                } else {
                    { event -> plainText to event.toString() }
                },
                serializeState = if (stateSerializer != null) {
                    { state -> json to Json.encodeToString(stateSerializer, state) }
                } else {
                    { state -> plainText to state.toString() }
                },
                deserializeInput = if (inputsSerializer != null) {
                    { contentType: ContentType, serializedInput: String ->
                        check(contentType == json)
                        Json.decodeFromString(inputsSerializer, serializedInput)
                    }
                } else {
                    null
                },
                deserializeState = if (stateSerializer != null) {
                    { contentType: ContentType, serializedState: String ->
                        check(contentType == json)
                        Json.decodeFromString(stateSerializer, serializedState)
                    }
                } else {
                    null
                },
            )
        }
    }
}
