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
    private val adapter: DebuggerAdapter<Inputs, Events, State> = ToStringDebuggerAdapter(),
) : BallastInterceptor<Inputs, Events, State> {

    override fun BallastInterceptorScope<Inputs, Events, State>.start(notifications: Flow<BallastNotification<Inputs, Events, State>>) {
        with(connection) {
            connectViewModel(
                BallastDebuggerViewModelConnection(
                    notifications = notifications,
                    viewModelName = hostViewModelName,
                    adapter = adapter,
                )
            )
        }
    }

    override fun toString(): String {
        return "BallastDebuggerInterceptor"
    }

    public companion object {

        public operator fun <Inputs : Any, Events : Any, State : Any> invoke(
            connection: BallastDebuggerClientConnection<*>,
            serializeInput: (Inputs) -> Pair<ContentType, String>,
            serializeEvent: (Events) -> Pair<ContentType, String>,
            serializeState: (State) -> Pair<ContentType, String>,
        ): BallastDebuggerInterceptor<Inputs, Events, State> {
            return BallastDebuggerInterceptor(
                connection,
                LambdaDebuggerAdapter(
                    serializeInput = serializeInput,
                    serializeEvent = serializeEvent,
                    serializeState = serializeState,
                )
            )
        }

        public operator fun <Inputs : Any, Events : Any, State : Any> invoke(
            connection: BallastDebuggerClientConnection<*>,
            inputsSerializer: KSerializer<Inputs>? = null,
            eventsSerializer: KSerializer<Events>? = null,
            stateSerializer: KSerializer<State>? = null,
            json: Json = Json,
        ): BallastDebuggerInterceptor<Inputs, Events, State> {
            return BallastDebuggerInterceptor(
                connection,
                JsonDebuggerAdapter(
                    inputsSerializer = inputsSerializer,
                    eventsSerializer = eventsSerializer,
                    stateSerializer = stateSerializer,
                    json = json,
                )
            )
        }
    }
}
