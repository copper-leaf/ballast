package com.copperleaf.ballast.debugger.server.vm

import com.copperleaf.ballast.debugger.models.BallastApplicationState
import com.copperleaf.ballast.debugger.server.BallastDebuggerServerSettings
import com.copperleaf.ballast.debugger.versions.v3.BallastDebuggerActionV3
import com.copperleaf.ballast.debugger.versions.v3.BallastDebuggerEventV3
import io.github.copper_leaf.ballast_debugger_server.BALLAST_VERSION
import kotlinx.coroutines.flow.MutableSharedFlow

object DebuggerServerContract {
    data class State(
        val port: Int = 0,
        val actions: MutableSharedFlow<BallastDebuggerActionV3> = MutableSharedFlow(extraBufferCapacity = Int.MAX_VALUE),

        val allMessages: List<BallastDebuggerEventV3> = emptyList(),
        val ballastVersion: String = BALLAST_VERSION,
        val applicationState: BallastApplicationState = BallastApplicationState(),
    ) {
        override fun toString(): String {
            return "State(${applicationState.connections.size} connections)"
        }
    }

    sealed class Inputs {
        data class StartServer(val settings: BallastDebuggerServerSettings) : Inputs()

        class ConnectionEstablished(val connectionId: String, val connectionBallastVersion: String) : Inputs()

        object ClearAll : Inputs()

        data class ClearConnection(val connectionId: String) : Inputs()
        data class RemoveConnection(val connectionId: String) : Inputs()

        data class ClearViewModel(val connectionId: String, val viewModelName: String) : Inputs()

        data class DebuggerEventReceived(val message: BallastDebuggerEventV3) : Inputs()
        data class SendDebuggerAction(val action: BallastDebuggerActionV3) : Inputs()
    }

    sealed class Events {
        data class ConnectionEstablished(val connectionId: String): Events()
    }
}
