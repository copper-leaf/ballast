package com.copperleaf.ballast.debugger.server.vm

import com.copperleaf.ballast.debugger.models.BallastApplicationState
import com.copperleaf.ballast.debugger.server.BallastDebuggerServerSettings
import com.copperleaf.ballast.debugger.versions.v5.BallastDebuggerActionV5
import com.copperleaf.ballast.debugger.versions.v5.BallastDebuggerEventV5
import io.github.copperleaf.ballastdebuggerserver.BALLAST_VERSION
import kotlinx.coroutines.flow.MutableSharedFlow

public object DebuggerServerContract {
    public data class State(
        val port: Int = 0,
        val actions: MutableSharedFlow<BallastDebuggerActionV5> = MutableSharedFlow(extraBufferCapacity = Int.MAX_VALUE),

        val allMessages: List<BallastDebuggerEventV5> = emptyList(),
        val ballastVersion: String = BALLAST_VERSION,
        val applicationState: BallastApplicationState = BallastApplicationState(),
    ) {
        override fun toString(): String {
            return "State(${applicationState.connections.size} connections)"
        }
    }

    public sealed interface Inputs {
        public data class StartServer(val settings: BallastDebuggerServerSettings) : Inputs

        public data class ConnectionEstablished(val connectionId: String, val connectionBallastVersion: String) : Inputs

        public object ClearAll : Inputs

        public data class ClearConnection(val connectionId: String) : Inputs
        public data class RemoveConnection(val connectionId: String) : Inputs

        public data class ClearViewModel(val connectionId: String, val viewModelName: String) : Inputs

        public data class DebuggerEventReceived(val message: BallastDebuggerEventV5) : Inputs
        public data class SendDebuggerAction(val action: BallastDebuggerActionV5) : Inputs

        public data object ClearAllConnections : Inputs
        public data class ClearAllStates(val connectionId: String, val viewModelName: String) : Inputs
        public data class ClearAllInputs(val connectionId: String, val viewModelName: String) : Inputs
        public data class ClearAllEvents(val connectionId: String, val viewModelName: String) : Inputs
        public data class ClearAllSideJobs(val connectionId: String, val viewModelName: String) : Inputs
        public data class ClearAllLogs(val connectionId: String, val viewModelName: String) : Inputs
    }

    public sealed interface Events {
        public data class ConnectionEstablished(val connectionId: String) : Events
    }
}
