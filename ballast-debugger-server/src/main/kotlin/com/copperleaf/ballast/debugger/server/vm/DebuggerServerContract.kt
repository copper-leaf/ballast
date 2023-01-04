package com.copperleaf.ballast.debugger.server.vm

import com.copperleaf.ballast.debugger.models.BallastApplicationState
import com.copperleaf.ballast.debugger.models.BallastDebuggerAction
import com.copperleaf.ballast.debugger.models.BallastDebuggerEvent
import com.copperleaf.ballast.debugger.server.BallastDebuggerServerSettings
import io.github.copper_leaf.ballast_debugger_server.BALLAST_VERSION
import kotlinx.coroutines.flow.MutableSharedFlow

object DebuggerServerContract {
    data class State(
        val port: Int = 0,
        val actions: MutableSharedFlow<BallastDebuggerAction> = MutableSharedFlow(extraBufferCapacity = Int.MAX_VALUE),

        val allMessages: List<BallastDebuggerEvent> = emptyList(),
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

        data class DebuggerEventReceived(val message: BallastDebuggerEvent) : Inputs()
        data class SendDebuggerAction(val action: BallastDebuggerAction) : Inputs()
    }

    sealed class Events {
        data class ConnectionEstablished(val connectionId: String): Events()
    }
}
