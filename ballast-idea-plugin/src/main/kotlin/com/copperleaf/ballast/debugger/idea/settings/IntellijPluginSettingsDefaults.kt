package com.copperleaf.ballast.debugger.idea.settings

import com.copperleaf.ballast.debugger.idea.ui.debugger.router.DebuggerRoute

/**
 * A read-only snapshot of settings, as captured at a specific point in time. Changes to the underlying preferences
 * will not update this object, the screen reading from these settings must be re-created to receive updates.
 */
data class IntellijPluginSettingsDefaults(
    override val darkTheme: Boolean = true,
    override val debuggerServerPort: Int = 9684,
    override val lastRoute: DebuggerRoute = DebuggerRoute.Connection,
    override val lastViewModelName: String = "",
    override val autoselectDebuggerConnections: Boolean = true,
    override val alwaysShowCurrentState: Boolean = true,
    override val showCurrentRoute: Boolean = true,
    override val routerViewModelName: String = "Router",
    override val detailsPanePercentage: Float = 0.35f,
) : IntellijPluginSettings