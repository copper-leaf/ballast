package com.copperleaf.ballast.debugger.idea.settings

import com.copperleaf.ballast.debugger.idea.features.debugger.router.DebuggerRoute
import com.copperleaf.ballast.debugger.idea.features.templates.BallastViewModel
import com.copperleaf.ballast.debugger.idea.repository.RepositoryViewModel
import com.copperleaf.ballast.debugger.idea.utils.PropertiesComponentSettings
import com.copperleaf.ballast.debugger.idea.utils.enum
import com.intellij.ide.util.PropertiesComponent
import com.russhwolf.settings.Settings
import com.russhwolf.settings.boolean
import com.russhwolf.settings.float
import com.russhwolf.settings.int
import com.russhwolf.settings.string

/**
 * The global settings store for the entire Ballast plugin. It is backed by IntelliJ's PropertiesComponent.
 *
 * Do not reference this class directly, either for reading or updating preferences. Instead, connect the Ui ViewModel
 * to [RepositoryViewModel] and observe its state to read saved settings, and sent Inputs to the Repository to update
 * the settings.
 *
 * See https://plugins.jetbrains.com/docs/intellij/persisting-state-of-components.html#implementing-the-persistentstatecomponent-interface
 */
class IntellijPluginPersistentSettings :
    IntellijPluginMutableSettings,
    Settings by PropertiesComponentSettings("BALLAST", PropertiesComponent.getInstance()) {
    private val defaults = IntellijPluginSettingsDefaults()

// GeneralSettings
// ---------------------------------------------------------------------------------------------------------------------

    override val ballastVersion: String get() = defaults.ballastVersion
    override var darkTheme: Boolean by boolean(null, defaults.darkTheme)

// BallastDebuggerServerSettings
// ---------------------------------------------------------------------------------------------------------------------

    override var debuggerServerPort: Int by int(null, defaults.debuggerServerPort)

// DebuggerUiSettings
// ---------------------------------------------------------------------------------------------------------------------

    override var lastRoute: DebuggerRoute by enum(null, defaults.lastRoute, DebuggerRoute::valueOf)
    override var lastViewModelName: String by string(null, defaults.lastViewModelName)
    override var autoselectDebuggerConnections: Boolean by boolean(null, defaults.autoselectDebuggerConnections)
    override var alwaysShowCurrentState: Boolean by boolean(null, defaults.alwaysShowCurrentState)
    override var showCurrentRoute: Boolean by boolean(null, defaults.showCurrentRoute)
    override var routerViewModelName: String by string(null, defaults.routerViewModelName)
    override var detailsPanePercentage: Float by float(null, defaults.detailsPanePercentage)

// TemplatesSettings
// ---------------------------------------------------------------------------------------------------------------------

    override var baseViewModelType: BallastViewModel.ViewModelTemplate by enum(null, defaults.baseViewModelType, BallastViewModel.ViewModelTemplate::valueOf)
    override var allComponentsIncludesViewModel: Boolean by boolean(null, defaults.allComponentsIncludesViewModel)
    override var allComponentsIncludesSavedStateAdapter: Boolean by boolean(null, defaults.allComponentsIncludesSavedStateAdapter)
    override var defaultVisibility: BallastViewModel.DefaultVisibility by enum(null, defaults.defaultVisibility, BallastViewModel.DefaultVisibility::valueOf)
    override var useDataObjects: Boolean by boolean(null, defaults.useDataObjects)

    fun applyFromSnapshot(settings: IntellijPluginSettings) {
        // GeneralSettings
        this.darkTheme = settings.darkTheme

        // BallastDebuggerServerSettings
        this.debuggerServerPort = settings.debuggerServerPort

        // DebuggerUiSettings
        this.lastRoute = settings.lastRoute
        this.lastViewModelName = settings.lastViewModelName
        this.autoselectDebuggerConnections = settings.autoselectDebuggerConnections
        this.alwaysShowCurrentState = settings.alwaysShowCurrentState
        this.showCurrentRoute = settings.showCurrentRoute
        this.routerViewModelName = settings.routerViewModelName
        this.detailsPanePercentage = settings.detailsPanePercentage

        // TemplatesSettings
        this.baseViewModelType = settings.baseViewModelType
        this.allComponentsIncludesViewModel = settings.allComponentsIncludesViewModel
        this.allComponentsIncludesSavedStateAdapter = settings.allComponentsIncludesSavedStateAdapter
        this.defaultVisibility = settings.defaultVisibility
        this.useDataObjects = settings.useDataObjects
    }
}
