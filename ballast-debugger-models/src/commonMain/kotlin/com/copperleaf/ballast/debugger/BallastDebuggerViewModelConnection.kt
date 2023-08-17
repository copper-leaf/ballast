package com.copperleaf.ballast.debugger

import com.copperleaf.ballast.BallastNotification
import com.copperleaf.ballast.debugger.models.serialize
import com.copperleaf.ballast.debugger.versions.v4.BallastDebuggerEventV4
import io.ktor.http.ContentType
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDateTime

public const val CONNECTION_ID_HEADER: String = "x-ballast-connection-id"
public const val BALLAST_VERSION_HEADER: String = "x-ballast-version"

public data class BallastDebuggerViewModelConnection<Inputs : Any, Events : Any, State : Any>(
    public val viewModelName: String,
    public val notifications: Flow<BallastNotification<Inputs, Events, State>>,
    public val serializeInput: (Inputs) -> Pair<ContentType, String>,
    public val serializeEvent: (Events) -> Pair<ContentType, String>,
    public val serializeState: (State) -> Pair<ContentType, String>,
)

public class BallastDebuggerOutgoingEventWrapper<Inputs : Any, Events : Any, State : Any>(
    public val connection: BallastDebuggerViewModelConnection<Inputs, Events, State>,
    public val notification: BallastNotification<Inputs, Events, State>?,
    public val debuggerEvent: BallastDebuggerEventV4?,
    public val updateConnectionState: Boolean,
) {
    public fun serialize(
        connectionId: String,
        uuid: String,
        firstSeen: LocalDateTime,
        now: LocalDateTime,
    ): BallastDebuggerEventV4 {
        return notification!!.serialize(
            connectionId = connectionId,
            viewModelConnection = connection,
            uuid = uuid,
            firstSeen = firstSeen,
            now = now,
        )
    }
}
