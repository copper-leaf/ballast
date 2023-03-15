package com.copperleaf.ballast.debugger.idea.settings

import com.copperleaf.ballast.debugger.idea.repository.RepositoryViewModel
import com.copperleaf.ballast.debugger.idea.ui.debugger.router.DebuggerRoute
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
class IntellijPluginPersistentSettings : IntellijPluginMutableSettings,
    Settings by PropertiesComponentSettings("BALLAST", PropertiesComponent.getInstance()) {

    private val defaults = IntellijPluginSettingsDefaults()

    override val ballastVersion: String get() = defaults.ballastVersion
    override var darkTheme: Boolean by boolean(null, defaults.darkTheme)
    override var debuggerServerPort: Int by int(null, defaults.debuggerServerPort)
    override var lastRoute: DebuggerRoute by enum(null, defaults.lastRoute, DebuggerRoute::valueOf)
    override var lastViewModelName: String by string(null, defaults.lastViewModelName)
    override var autoselectDebuggerConnections: Boolean by boolean(null, defaults.autoselectDebuggerConnections)
    override var alwaysShowCurrentState: Boolean by boolean(null, defaults.alwaysShowCurrentState)
    override var showCurrentRoute: Boolean by boolean(null, defaults.showCurrentRoute)
    override var routerViewModelName: String by string(null, defaults.routerViewModelName)
    override var detailsPanePercentage: Float by float(null, defaults.detailsPanePercentage)
}
