package com.copperleaf.ballast.debugger.idea.settings

import com.copperleaf.ballast.debugger.idea.features.debugger.router.DebuggerRoute
import com.copperleaf.ballast.debugger.idea.features.templates.BallastViewModel

/**
 * A mutable version of [IntellijPluginSettings].
 */
interface IntellijPluginMutableSettings : IntellijPluginSettings {

// GeneralSettings
// ---------------------------------------------------------------------------------------------------------------------

    override var darkTheme: Boolean

// BallastDebuggerServerSettings
// ---------------------------------------------------------------------------------------------------------------------

    override var debuggerServerPort: Int

// DebuggerUiSettings
// ---------------------------------------------------------------------------------------------------------------------

    override var lastRoute: DebuggerRoute
    override var lastViewModelName: String
    override var autoselectDebuggerConnections: Boolean
    override var alwaysShowCurrentState: Boolean
    override var showCurrentRoute: Boolean
    override var routerViewModelName: String
    override var detailsPanePercentage: Float

// TemplatesSettings
// ---------------------------------------------------------------------------------------------------------------------

    override var baseViewModelType: BallastViewModel.ViewModelTemplate
    override var allComponentsIncludesViewModel: Boolean
    override var allComponentsIncludesSavedStateAdapter: Boolean
    override var allComponentsIncludesComposeUi: Boolean
    override var defaultVisibility: BallastViewModel.DefaultVisibility
    override var useDataObjects: Boolean
}
