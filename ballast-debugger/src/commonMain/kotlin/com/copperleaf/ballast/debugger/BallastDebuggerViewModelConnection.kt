package com.copperleaf.ballast.debugger

import com.copperleaf.ballast.BallastNotification
import com.copperleaf.ballast.debugger.models.serialize
import com.copperleaf.ballast.debugger.versions.v3.BallastDebuggerEventV3
import io.ktor.http.ContentType
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDateTime

internal data class BallastDebuggerViewModelConnection<Inputs : Any, Events : Any, State : Any>(
    val viewModelName: String,
    val notifications: Flow<BallastNotification<Inputs, Events, State>>,
    val serializeInput: (Inputs) -> Pair<ContentType, String>,
    val serializeEvent: (Events) -> Pair<ContentType, String>,
    val serializeState: (State) -> Pair<ContentType, String>,
)

internal class BallastDebuggerOutgoingEventWrapper<Inputs : Any, Events : Any, State : Any>(
    val connection: BallastDebuggerViewModelConnection<Inputs, Events, State>,
    val notification: BallastNotification<Inputs, Events, State>?,
    val debuggerEvent: BallastDebuggerEventV3?,
    val updateConnectionState: Boolean,
) {
    fun serialize(
        connectionId: String,
        uuid: String,
        firstSeen: LocalDateTime,
        now: LocalDateTime,
    ): BallastDebuggerEventV3 {
        return notification!!.serialize(
            connectionId = connectionId,
            viewModelConnection = connection,
            uuid = uuid,
            firstSeen = firstSeen,
            now = now,
        )
    }
}
