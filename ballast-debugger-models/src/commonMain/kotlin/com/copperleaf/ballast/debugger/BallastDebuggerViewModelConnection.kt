package com.copperleaf.ballast.debugger

import com.copperleaf.ballast.BallastEncoder
import com.copperleaf.ballast.BallastNotification
import com.copperleaf.ballast.debugger.models.serialize
import com.copperleaf.ballast.debugger.versions.v5.BallastDebuggerEventV5
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDateTime

public const val CONNECTION_ID_HEADER: String = "x-ballast-connection-id"
public const val BALLAST_VERSION_HEADER: String = "x-ballast-version"

@Suppress("DEPRECATION")
public data class BallastDebuggerViewModelConnection<Inputs : Any, Events : Any, State : Any>(
    public val viewModelName: String,
    public val notifications: Flow<BallastNotification<Inputs, Events, State>>,
    public val adapter: DebuggerAdapter<Inputs, Events, State>?
)

public class BallastDebuggerOutgoingEventWrapper<Inputs : Any, Events : Any, State : Any>(
    public val connection: BallastDebuggerViewModelConnection<Inputs, Events, State>,
    public val notification: BallastNotification<Inputs, Events, State>?,
    public val debuggerEvent: BallastDebuggerEventV5?,
    public val updateConnectionState: Boolean,
    private val ballastEncoder: BallastEncoder<Inputs, Events, State>,
) {
    public fun serialize(
        connectionId: String,
        uuid: String,
        firstSeen: LocalDateTime,
        now: LocalDateTime,
    ): BallastDebuggerEventV5 {
        return notification!!.serialize(
            connectionId = connectionId,
            viewModelConnection = connection,
            uuid = uuid,
            firstSeen = firstSeen,
            now = now,
            ballastEncoder = ballastEncoder,
        )
    }
}
