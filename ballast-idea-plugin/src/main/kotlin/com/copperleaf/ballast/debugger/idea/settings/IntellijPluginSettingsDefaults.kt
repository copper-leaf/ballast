package com.copperleaf.ballast.debugger.idea.settings

import com.copperleaf.ballast.debugger.idea.features.debugger.router.DebuggerRoute
import com.copperleaf.ballast.debugger.idea.features.templates.BallastViewModel
import io.github.copper_leaf.ballast_idea_plugin.BALLAST_VERSION

/**
 * A read-only snapshot of settings, as captured at a specific point in time. Changes to the underlying preferences
 * will not update this object, the screen reading from these settings must be re-created to receive updates.
 */
data class IntellijPluginSettingsDefaults(
    // GeneralSettings
    override val ballastVersion: String = BALLAST_VERSION,
    override val darkTheme: Boolean = true,

    // BallastDebuggerServerSettings
    override val debuggerServerPort: Int = 9684,

    // DebuggerUiSettings
    override val lastRoute: DebuggerRoute = DebuggerRoute.Connection,
    override val lastViewModelName: String = "",
    override val autoselectDebuggerConnections: Boolean = true,
    override val alwaysShowCurrentState: Boolean = true,
    override val showCurrentRoute: Boolean = true,
    override val routerViewModelName: String = "Router",
    override val detailsPanePercentage: Float = 0.35f,

    // TemplatesSettings
    override val baseViewModelType: BallastViewModel.ViewModelTemplate = BallastViewModel.ViewModelTemplate.Basic,
    override val allComponentsIncludesViewModel: Boolean = true,
    override val allComponentsIncludesSavedStateAdapter: Boolean = false,
    override val defaultVisibility: BallastViewModel.DefaultVisibility = BallastViewModel.DefaultVisibility.Default,
    override val useDataObjects: Boolean = false,
) : IntellijPluginSettings
