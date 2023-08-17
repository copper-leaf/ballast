package com.copperleaf.ballast.debugger.idea.settings

import com.copperleaf.ballast.debugger.idea.features.debugger.router.DebuggerRoute
import com.copperleaf.ballast.debugger.idea.features.templates.BallastViewModel

/**
 * A read-only snapshot of settings, as captured at a specific point in time. Changes to the underlying preferences
 * will not update this object, the screen reading from these settings must be re-created to receive updates.
 */
data class IntellijPluginSettingsSnapshot(
    // GeneralSettings
    override val ballastVersion: String,
    override val darkTheme: Boolean,

    // BallastDebuggerServerSettings
    override val debuggerServerPort: Int,

    // DebuggerUiSettings
    override val lastRoute: DebuggerRoute,
    override val lastViewModelName: String,
    override val autoselectDebuggerConnections: Boolean,
    override val alwaysShowCurrentState: Boolean,
    override val showCurrentRoute: Boolean,
    override val routerViewModelName: String,
    override val detailsPanePercentage: Float,

    // TemplatesSettings
    override val baseViewModelType: BallastViewModel.ViewModelTemplate,
    override val allComponentsIncludesViewModel: Boolean,
    override val allComponentsIncludesSavedStateAdapter: Boolean,
    override val defaultVisibility: BallastViewModel.DefaultVisibility,
    override val useDataObjects: Boolean,
) : IntellijPluginSettings {

    companion object {
        fun defaults(): IntellijPluginSettingsSnapshot {
            return fromSettings(IntellijPluginSettingsDefaults())
        }
        fun fromSettings(settings: IntellijPluginSettings): IntellijPluginSettingsSnapshot {
            return IntellijPluginSettingsSnapshot(
                // GeneralSettings
                ballastVersion = settings.ballastVersion,
                darkTheme = settings.darkTheme,

                // BallastDebuggerServerSettings
                debuggerServerPort = settings.debuggerServerPort,

                // DebuggerUiSettings
                lastRoute = settings.lastRoute,
                lastViewModelName = settings.lastViewModelName,
                autoselectDebuggerConnections = settings.autoselectDebuggerConnections,
                alwaysShowCurrentState = settings.alwaysShowCurrentState,
                showCurrentRoute = settings.showCurrentRoute,
                routerViewModelName = settings.routerViewModelName,
                detailsPanePercentage = settings.detailsPanePercentage,

                // TemplatesSettings
                baseViewModelType = settings.baseViewModelType,
                allComponentsIncludesViewModel = settings.allComponentsIncludesViewModel,
                allComponentsIncludesSavedStateAdapter = settings.allComponentsIncludesSavedStateAdapter,
                defaultVisibility = settings.defaultVisibility,
                useDataObjects = settings.useDataObjects,
            )
        }
    }
}
