package com.copperleaf.ballast.debugger.idea.features.debugger.vm

import com.copperleaf.ballast.debugger.idea.features.debugger.router.DebuggerRoute
import com.copperleaf.ballast.debugger.idea.settings.DebuggerUiSettings
import com.copperleaf.ballast.debugger.idea.settings.GeneralSettings
import com.copperleaf.ballast.debugger.models.BallastApplicationState
import com.copperleaf.ballast.debugger.server.BallastDebuggerServerSettings
import com.copperleaf.ballast.debugger.server.vm.DebuggerServerContract
import com.copperleaf.ballast.navigation.routing.Backstack
import com.copperleaf.ballast.repository.cache.Cached
import com.copperleaf.ballast.repository.cache.getCachedOrThrow
import com.copperleaf.ballast.repository.cache.isReady

public object DebuggerUiContract {
    public data class State(
        val cachedGeneralSettings: Cached<GeneralSettings> = Cached.NotLoaded(),
        val cachedBallastDebuggerServerSettings: Cached<BallastDebuggerServerSettings> = Cached.NotLoaded(),
        val cachedDebuggerUiSettings: Cached<DebuggerUiSettings> = Cached.NotLoaded(),

        val serverState: BallastApplicationState = BallastApplicationState(),
        val backstack: Backstack<DebuggerRoute> = emptyList(),
        val searchText: String = "",

//        val connectionsPanePercentage: SplitPaneState = SplitPaneState(0.35f, true),
//        val viewModelsPanePercentage: SplitPaneState = SplitPaneState(0.35f, true),
//        val eventsPanePercentage: SplitPaneState = SplitPaneState(0.35f, true),
    ) {
        public val isReady: Boolean = isReady(
            cachedGeneralSettings,
            cachedBallastDebuggerServerSettings,
            cachedDebuggerUiSettings,
        )
        public val generalSettings: GeneralSettings by lazy { cachedGeneralSettings.getCachedOrThrow() }
        public val ballastDebuggerServerSettings: BallastDebuggerServerSettings by lazy { cachedBallastDebuggerServerSettings.getCachedOrThrow() }
        public val debuggerUiSettings: DebuggerUiSettings by lazy { cachedDebuggerUiSettings.getCachedOrThrow() }
    }

    public sealed interface Inputs {
        public data object Initialize : Inputs

        public data class OnConnectionEstablished(val connectionId: String) : Inputs

        public data class ServerStateChanged(val serverState: BallastApplicationState) : Inputs
        public data class BackstackChanged(val backstack: Backstack<DebuggerRoute>) : Inputs
        public data class GeneralSettingsChanged(val settings: Cached<GeneralSettings>) : Inputs
        public data class BallastDebuggerServerSettingsChanged(val settings: Cached<BallastDebuggerServerSettings>) : Inputs
        public data class DebuggerUiSettingsChanged(val settings: Cached<DebuggerUiSettings>) : Inputs

        // forwarded to other VMs
        public data class SendToDebuggerServer(val debuggerServerInput: DebuggerServerContract.Inputs) : Inputs
        public data class Navigate(val destinationUrl: String) : Inputs

        // manage own interactions
        public data class UpdateSearchText(val value: String) : Inputs
        public data class CopyToClipboard(val text: String) : Inputs
    }

    public sealed interface Events {
        public data class CopyToClipboard(val text: String) : Events
    }
}
