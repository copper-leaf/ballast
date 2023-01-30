package com.copperleaf.ballast.debugger.idea.settings

import com.copperleaf.ballast.debugger.idea.ui.debugger.router.DebuggerRoute

/**
 * A mutable version of [BallastIntellijPluginSettings].
 */
interface BallastIntellijPluginMutableSettings : BallastIntellijPluginSettings {
    override var darkTheme: Boolean

    override var debuggerServerPort: Int

    override var lastRoute: DebuggerRoute
    override var lastViewModelName: String
    override var autoselectDebuggerConnections: Boolean
    override var alwaysShowCurrentState: Boolean
    override var showCurrentRoute: Boolean
    override var routerViewModelName: String
    override var detailsPanePercentage: Float
}
