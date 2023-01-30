package com.copperleaf.ballast.debugger.models

import com.copperleaf.ballast.debugger.utils.now
import kotlinx.datetime.LocalDateTime

public data class BallastApplicationState(
    public val connections: List<BallastConnectionState> = emptyList(),
) {
    public fun updateConnection(
        connectionId: String,
        block: BallastConnectionState.() -> BallastConnectionState,
    ): BallastApplicationState {
        val indexOfConnection = connections.indexOfFirst { it.connectionId == connectionId }

        return this.copy(
            connections = connections
                .toMutableList()
                .apply {
                    if (indexOfConnection != -1) {
                        // we're updating a value in an existing connection
                        this[indexOfConnection] = this[indexOfConnection].block().copy(lastSeen = LocalDateTime.now())
                    } else {
                        // this is the first time we're seeing this connection, create a new entry for it
                        this.add(0, BallastConnectionState(connectionId, firstSeen  = LocalDateTime.now()).block())
                    }
                }
                .toList(),
        )
    }

    public fun removeConnection(
        connectionId: String,
    ): BallastApplicationState {
        val indexOfConnection = connections.indexOfFirst { it.connectionId == connectionId }

        return this.copy(
            connections = connections
                .toMutableList()
                .apply {
                    if (indexOfConnection != -1) {
                        removeAt(indexOfConnection)
                    }
                }
                .toList(),
        )
    }
}
