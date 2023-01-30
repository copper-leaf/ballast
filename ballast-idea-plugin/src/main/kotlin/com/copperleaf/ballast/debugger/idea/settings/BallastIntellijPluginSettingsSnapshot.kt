package com.copperleaf.ballast.debugger.idea.settings

import com.copperleaf.ballast.debugger.idea.ui.debugger.router.DebuggerRoute

/**
 * A read-only snapshot of settings, as captured at a specific point in time. Changes to the underlying preferences
 * will not update this object, the screen reading from these settings must be re-created to receive updates.
 */
data class BallastIntellijPluginSettingsSnapshot(
    override val darkTheme: Boolean,
    override val debuggerServerPort: Int,
    override val lastRoute: DebuggerRoute,
    override val lastViewModelName: String,
    override val autoselectDebuggerConnections: Boolean,
    override val alwaysShowCurrentState: Boolean,
    override val showCurrentRoute: Boolean,
    override val routerViewModelName: String,
    override val detailsPanePercentage: Float,
) : BallastIntellijPluginSettings
