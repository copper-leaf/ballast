package com.copperleaf.ballast.debugger.idea.ui.debugger

import com.copperleaf.ballast.debugger.idea.settings.BallastIntellijPluginSettingsSnapshot
import com.copperleaf.ballast.debugger.idea.ui.debugger.router.DebuggerRoute
import com.copperleaf.ballast.debugger.idea.ui.debugger.widgets.ViewModelContentTab
import com.copperleaf.ballast.debugger.models.BallastApplicationState
import com.copperleaf.ballast.debugger.models.BallastConnectionState
import com.copperleaf.ballast.debugger.models.BallastDebuggerAction
import com.copperleaf.ballast.debugger.models.BallastEventState
import com.copperleaf.ballast.debugger.models.BallastInputState
import com.copperleaf.ballast.debugger.models.BallastSideJobState
import com.copperleaf.ballast.debugger.models.BallastStateSnapshot
import com.copperleaf.ballast.debugger.models.BallastViewModelState
import com.copperleaf.ballast.debugger.server.vm.DebuggerServerContract
import com.copperleaf.ballast.navigation.routing.Backstack
import com.copperleaf.ballast.navigation.routing.RouterContract
import com.copperleaf.ballast.navigation.routing.currentRouteOrNull
import org.jetbrains.compose.splitpane.SplitPaneState

object DebuggerUiContract {
    data class State(
        val uiSettings: BallastIntellijPluginSettingsSnapshot,
        val serverState: BallastApplicationState = BallastApplicationState(),
        val backstack: Backstack<DebuggerRoute> = emptyList(),
        val searchText: String = "",
        val connectionsPanePercentage: SplitPaneState = SplitPaneState(0.35f, true),
        val viewModelsPanePercentage: SplitPaneState = SplitPaneState(0.35f, true),
        val eventsPanePercentage: SplitPaneState = SplitPaneState(0.35f, true),
    ) {
        val ballastVersion: String = "2.3.1-SNAPSHOT"
        val port: Int = uiSettings.debuggerServerPort
        val applicationState: BallastApplicationState = serverState

        val focusedConnectionId: String? = null
        val focusedConnection: BallastConnectionState? = null

        val focusedViewModelName: String? = null
        val focusedViewModel: BallastViewModelState? = null

        val focusedDebuggerEventUuid: String? = null
        val focusedViewModelEvent: BallastEventState? = null
        val focusedViewModelInput: BallastInputState? = null
        val focusedViewModelStateSnapshot: BallastStateSnapshot? = null
        val focusedViewModelSideJob: BallastSideJobState? = null

        val selectedViewModelContentTab: ViewModelContentTab? = when(backstack.currentRouteOrNull) {
            DebuggerRoute.Connection -> null
            DebuggerRoute.ViewModelStates -> ViewModelContentTab.States
            DebuggerRoute.ViewModelStateDetails -> ViewModelContentTab.States
            DebuggerRoute.ViewModelInputs -> ViewModelContentTab.Inputs
            DebuggerRoute.ViewModelInputDetails -> ViewModelContentTab.Inputs
            DebuggerRoute.ViewModelEvents -> ViewModelContentTab.Events
            DebuggerRoute.ViewModelEventDetails -> ViewModelContentTab.Events
            DebuggerRoute.ViewModelSideJobs -> ViewModelContentTab.SideJobs
            DebuggerRoute.ViewModelSideJobDetails -> ViewModelContentTab.SideJobs
            DebuggerRoute.ViewModelInterceptors -> ViewModelContentTab.Interceptors
            DebuggerRoute.ViewModelInterceptorDetails -> ViewModelContentTab.Interceptors
            DebuggerRoute.ViewModelLogs -> null
            DebuggerRoute.ViewModelTimeline -> null
            null -> null
        }
    }

    sealed class Inputs {
        object Initialize : Inputs()

        data class ServerStateChanged(val serverState: BallastApplicationState) : Inputs()
        data class BackstackChanged(val backstack: Backstack<DebuggerRoute>) : Inputs()

        data class FocusConnection(val connectionId: String) : Inputs()
        data class ClearConnection(val connectionId: String) : Inputs()
        object ClearAllConnections : Inputs()

        data class FocusViewModel(val connectionId: String, val viewModelName: String) : Inputs()
        data class ClearViewModel(val connectionId: String, val viewModelName: String) : Inputs()

        data class FocusEvent(val connectionId: String, val viewModelName: String, val eventUuid: String) : Inputs()
        data class SendDebuggerAction(val action: BallastDebuggerAction) : Inputs()
        data class UpdateSelectedViewModelContentTab(val tab: ViewModelContentTab) : Inputs()

        data class Navigate(val destinationUrl: String) : Inputs()

        data class UpdateSearchText(val value: String) : Inputs()
    }

    sealed class Events {
        data class SendCommandToRouter(val input: RouterContract.Inputs<DebuggerRoute>) : Events()
        data class SendCommandToDebuggerServer(val input: DebuggerServerContract.Inputs) : Events()
    }
}
