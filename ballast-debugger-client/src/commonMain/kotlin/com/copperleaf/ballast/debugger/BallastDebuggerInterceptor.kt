package com.copperleaf.ballast.debugger

import com.copperleaf.ballast.BallastInterceptor
import com.copperleaf.ballast.BallastInterceptorScope
import com.copperleaf.ballast.BallastNotification
import io.ktor.http.ContentType
import kotlinx.coroutines.flow.Flow

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
}
