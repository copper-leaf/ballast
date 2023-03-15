package com.copperleaf.ballast.debugger.idea.ui.debugger.vm

import com.copperleaf.ballast.debugger.idea.settings.IntellijPluginSettingsSnapshot
import com.copperleaf.ballast.debugger.idea.ui.debugger.router.DebuggerRoute
import com.copperleaf.ballast.debugger.idea.ui.debugger.ui.widgets.ViewModelContentTab
import com.copperleaf.ballast.debugger.models.BallastApplicationState
import com.copperleaf.ballast.debugger.versions.v3.BallastDebuggerActionV3
import com.copperleaf.ballast.navigation.routing.Backstack
import com.copperleaf.ballast.repository.cache.Cached
import com.copperleaf.ballast.repository.cache.getCachedOrThrow
import com.copperleaf.ballast.repository.cache.isLoading
import org.jetbrains.compose.splitpane.SplitPaneState

object DebuggerUiContract {
    data class State(
        val cachedSettings: Cached<IntellijPluginSettingsSnapshot> = Cached.NotLoaded(),
        val serverState: BallastApplicationState = BallastApplicationState(),
        val backstack: Backstack<DebuggerRoute> = emptyList(),
        val searchText: String = "",

        val connectionsPanePercentage: SplitPaneState = SplitPaneState(0.35f, true),
        val viewModelsPanePercentage: SplitPaneState = SplitPaneState(0.35f, true),
        val eventsPanePercentage: SplitPaneState = SplitPaneState(0.35f, true),
    ) {
        val isReady = !cachedSettings.isLoading()
        val settings by lazy {
            cachedSettings.getCachedOrThrow()
        }
    }

    sealed class Inputs {
        object Initialize : Inputs()

        data class OnConnectionEstablished(val connectionId: String) : Inputs()

        data class ServerStateChanged(val serverState: BallastApplicationState) : Inputs()
        data class BackstackChanged(val backstack: Backstack<DebuggerRoute>) : Inputs()
        data class SettingsChanged(val settings: Cached<IntellijPluginSettingsSnapshot>) : Inputs()

        object ClearAllConnections : Inputs()
        data class Navigate(val destinationUrl: String) : Inputs()

        data class SendDebuggerAction(val action: BallastDebuggerActionV3) : Inputs()
        data class UpdateSelectedViewModelContentTab(val tab: ViewModelContentTab) : Inputs()

        data class UpdateSearchText(val value: String) : Inputs()
    }

    sealed class Events
}
