package com.copperleaf.ballast.debugger.ui.debugger

import com.copperleaf.ballast.debugger.models.BallastApplicationState
import com.copperleaf.ballast.debugger.models.BallastConnectionState
import com.copperleaf.ballast.debugger.models.BallastDebuggerAction
import com.copperleaf.ballast.debugger.models.BallastDebuggerEvent
import com.copperleaf.ballast.debugger.models.BallastEventState
import com.copperleaf.ballast.debugger.models.BallastInputState
import com.copperleaf.ballast.debugger.models.BallastSideJobState
import com.copperleaf.ballast.debugger.models.BallastStateSnapshot
import com.copperleaf.ballast.debugger.models.BallastViewModelState
import com.copperleaf.ballast.debugger.ui.widgets.ViewModelContentTab
import kotlinx.coroutines.flow.MutableSharedFlow
import org.jetbrains.compose.splitpane.SplitPaneState

object DebuggerContract {
    data class State constructor(
        val actions: MutableSharedFlow<BallastDebuggerAction> = MutableSharedFlow(extraBufferCapacity = Int.MAX_VALUE),

        val allMessages: List<BallastDebuggerEvent> = emptyList(),
        val applicationState: BallastApplicationState = BallastApplicationState(),

        val focusedConnectionId: String? = null,
        val focusedViewModelName: String? = null,
        val focusedDebuggerEventUuid: String? = null,

        val connectionsPanePercentage: SplitPaneState = SplitPaneState(0.30f, true),
        val viewModelsPanePercentage: SplitPaneState = SplitPaneState(0.35f, true),
        val eventsPanePercentage: SplitPaneState = SplitPaneState(0.45f, true),
        val selectedViewModelContentTab: ViewModelContentTab = ViewModelContentTab.Inputs,
    ) {
        val focusedConnection: BallastConnectionState? = applicationState
            .connections
            .firstOrNull { it.connectionId == focusedConnectionId }
        val focusedViewModel: BallastViewModelState? = focusedConnection
            ?.viewModels
            ?.firstOrNull { it.viewModelName == focusedViewModelName }
        val focusedViewModelEvent: BallastEventState? = focusedViewModel
            ?.events
            ?.firstOrNull { it.uuid == focusedDebuggerEventUuid }
        val focusedViewModelInput: BallastInputState? = focusedViewModel
            ?.inputs
            ?.firstOrNull { it.uuid == focusedDebuggerEventUuid }
        val focusedViewModelStateSnapshot: BallastStateSnapshot? = focusedViewModel
            ?.states
            ?.firstOrNull { it.uuid == focusedDebuggerEventUuid }
        val focusedViewModelSideJob: BallastSideJobState? = focusedViewModel
            ?.sideJobs
            ?.firstOrNull { it.uuid == focusedDebuggerEventUuid }

        override fun toString(): String {
            return "State()"
        }
    }

    sealed class Inputs {
        data class StartServer(val port: Int = 9684) : Inputs()

        class ConnectionEstablished(val connectionId: String, val connectionBallastVersion: String) : Inputs()

        data class FocusConnection(val connectionId: String) : Inputs()
        data class FocusViewModel(val connectionId: String, val viewModelName: String) : Inputs()
        data class FocusEvent(val connectionId: String, val viewModelName: String, val eventUuid: String) : Inputs()

        object ClearAll : Inputs()
        data class ClearConnection(val connectionId: String) : Inputs()
        data class ClearViewModel(val connectionId: String, val viewModelName: String) : Inputs()

        data class DebuggerEventReceived(val message: BallastDebuggerEvent) : Inputs()
        data class SendDebuggerAction(val action: BallastDebuggerAction) : Inputs()

        data class UpdateSelectedViewModelContentTab(val value: ViewModelContentTab) : Inputs()
    }

    sealed class Events
}
