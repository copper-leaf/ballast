package com.copperleaf.ballast.debugger.idea.settings

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
 * Do not reference this class directly, either for reading or updating preferences. Instead, use
 * [IntellijPluginPersistentSettings.edit] and [BallastIntellijPluginInMemorySettings.save] to modify settings,
 * or [IntellijPluginPersistentSettings.snapshot] to get a version of settings that can be read in the UI which
 * will not be changed unexpectedly.
 *
 * See https://plugins.jetbrains.com/docs/intellij/persisting-state-of-components.html#implementing-the-persistentstatecomponent-interface
 */
object IntellijPluginPersistentSettings : IntellijPluginMutableSettings,
    Settings by PropertiesComponentSettings("BALLAST", PropertiesComponent.getInstance()) {

    private const val darkThemeDefault: Boolean = true
    private const val debuggerServerPortDefault: Int = 9684
    private val lastRouteDefault: DebuggerRoute = DebuggerRoute.Connection
    private const val lastViewModelNameDefault: String = ""
    private const val autoselectDebuggerConnectionsDefault: Boolean = true
    private const val alwaysShowCurrentStateDefault: Boolean = true
    private const val showCurrentRouteDefault: Boolean = true
    private const val routerViewModelNameDefault: String = "Router"
    private const val detailsPanePercentageDefault: Float = 0.35f

    override var darkTheme: Boolean by boolean(null, darkThemeDefault)
    override var debuggerServerPort: Int by int(null, debuggerServerPortDefault)
    override var lastRoute: DebuggerRoute by enum(null, lastRouteDefault, DebuggerRoute::valueOf)
    override var lastViewModelName: String by string(null, lastViewModelNameDefault)
    override var autoselectDebuggerConnections: Boolean by boolean(null, autoselectDebuggerConnectionsDefault)
    override var alwaysShowCurrentState: Boolean by boolean(null, alwaysShowCurrentStateDefault)
    override var showCurrentRoute: Boolean by boolean(null, showCurrentRouteDefault)
    override var routerViewModelName: String by string(null, routerViewModelNameDefault)
    override var detailsPanePercentage: Float by float(null, detailsPanePercentageDefault)

    fun snapshot(): IntellijPluginSettingsSnapshot {
        return IntellijPluginSettingsSnapshot(
            darkTheme = this.darkTheme,
            debuggerServerPort = this.debuggerServerPort,
            lastRoute = this.lastRoute,
            lastViewModelName = this.lastViewModelName,
            autoselectDebuggerConnections = this.autoselectDebuggerConnections,
            alwaysShowCurrentState = this.alwaysShowCurrentState,
            showCurrentRoute = this.showCurrentRoute,
            routerViewModelName = this.routerViewModelName,
            detailsPanePercentage = this.detailsPanePercentage,
        )
    }

    fun defaults(): IntellijPluginSettingsSnapshot {
        return IntellijPluginSettingsSnapshot(
            darkTheme = darkThemeDefault,
            debuggerServerPort = debuggerServerPortDefault,
            lastRoute = lastRouteDefault,
            lastViewModelName = lastViewModelNameDefault,
            autoselectDebuggerConnections = autoselectDebuggerConnectionsDefault,
            alwaysShowCurrentState = alwaysShowCurrentStateDefault,
            showCurrentRoute = showCurrentRouteDefault,
            routerViewModelName = routerViewModelNameDefault,
            detailsPanePercentage = detailsPanePercentageDefault,
        )
    }

    fun updateFromSnapshot(snapshot: IntellijPluginSettingsSnapshot) {
        this.darkTheme = snapshot.darkTheme

        this.lastRoute = snapshot.lastRoute
        this.lastViewModelName = snapshot.lastViewModelName

        this.debuggerServerPort = snapshot.debuggerServerPort
        this.autoselectDebuggerConnections = snapshot.autoselectDebuggerConnections

        this.alwaysShowCurrentState = snapshot.alwaysShowCurrentState
        this.showCurrentRoute = snapshot.showCurrentRoute
        this.routerViewModelName = snapshot.routerViewModelName

        this.detailsPanePercentage = snapshot.detailsPanePercentage
    }
}
