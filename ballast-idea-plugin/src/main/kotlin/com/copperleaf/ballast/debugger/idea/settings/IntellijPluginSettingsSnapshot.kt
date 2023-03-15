package com.copperleaf.ballast.debugger.idea.settings

import com.copperleaf.ballast.debugger.idea.ui.debugger.router.DebuggerRoute

/**
 * A read-only snapshot of settings, as captured at a specific point in time. Changes to the underlying preferences
 * will not update this object, the screen reading from these settings must be re-created to receive updates.
 */
data class IntellijPluginSettingsSnapshot(
    override val ballastVersion: String,
    override val darkTheme: Boolean,
    override val debuggerServerPort: Int,
    override val lastRoute: DebuggerRoute,
    override val lastViewModelName: String,
    override val autoselectDebuggerConnections: Boolean,
    override val alwaysShowCurrentState: Boolean,
    override val showCurrentRoute: Boolean,
    override val routerViewModelName: String,
    override val detailsPanePercentage: Float,
) : IntellijPluginSettings {

    companion object {
        fun defaults(): IntellijPluginSettingsSnapshot {
            return fromSettings(IntellijPluginSettingsDefaults())
        }
        fun fromSettings(settings: IntellijPluginSettings): IntellijPluginSettingsSnapshot {
            return IntellijPluginSettingsSnapshot(
                ballastVersion = settings.ballastVersion,
                darkTheme = settings.darkTheme,
                debuggerServerPort = settings.debuggerServerPort,
                lastRoute = settings.lastRoute,
                lastViewModelName = settings.lastViewModelName,
                autoselectDebuggerConnections = settings.autoselectDebuggerConnections,
                alwaysShowCurrentState = settings.alwaysShowCurrentState,
                showCurrentRoute = settings.showCurrentRoute,
                routerViewModelName = settings.routerViewModelName,
                detailsPanePercentage = settings.detailsPanePercentage,
            )
        }
    }
}
