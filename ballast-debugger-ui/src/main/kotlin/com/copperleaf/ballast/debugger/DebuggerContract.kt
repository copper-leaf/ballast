package com.copperleaf.ballast.debugger

import com.copperleaf.ballast.debugger.models.BallastApplicationState
import com.copperleaf.ballast.debugger.models.BallastConnectionState
import com.copperleaf.ballast.debugger.models.BallastDebuggerAction
import com.copperleaf.ballast.debugger.models.BallastDebuggerEvent
import com.copperleaf.ballast.debugger.models.BallastViewModelState
import kotlinx.coroutines.flow.MutableSharedFlow

object DebuggerContract {
    data class State constructor(
        val actions: MutableSharedFlow<BallastDebuggerAction> = MutableSharedFlow(extraBufferCapacity = Int.MAX_VALUE),

        val allMessages: List<BallastDebuggerEvent> = emptyList(),
        val applicationState: BallastApplicationState = BallastApplicationState(),

        val focusedConnectionId: String? = null,
        val focusedViewModelName: String? = null,
        val focusedEventUuid: String? = null,
    ) {
        val focusedConnection: BallastConnectionState? = applicationState
            .connections
            .firstOrNull { it.connectionId == focusedConnectionId }
        val focusedViewModel: BallastViewModelState? = focusedConnection
            ?.viewModels
            ?.firstOrNull { it.viewModelName == focusedViewModelName }
        val focusedEvent: BallastDebuggerEvent? = null

        override fun toString(): String {
            return "State()"
        }
    }

    sealed class Inputs {
        data class StartServer(val port: Int = 8080) : Inputs()

        data class FocusConnection(val connectionId: String) : Inputs()
        data class FocusViewModel(val connectionId: String, val viewModelName: String) : Inputs()
        data class FocusEvent(val connectionId: String, val viewModelName: String, val eventUuid: String) : Inputs()

        object ClearAll : Inputs()
        data class ClearConnection(val connectionId: String) : Inputs()
        data class ClearViewModel(val connectionId: String, val viewModelName: String) : Inputs()

        data class DebuggerEventReceived(val message: BallastDebuggerEvent) : Inputs()
        data class SendDebuggerAction(val action: BallastDebuggerAction) : Inputs()
    }

    sealed class Events
}
