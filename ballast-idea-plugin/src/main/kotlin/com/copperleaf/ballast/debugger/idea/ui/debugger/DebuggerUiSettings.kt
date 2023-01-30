package com.copperleaf.ballast.debugger.idea.ui.debugger

import com.copperleaf.ballast.debugger.idea.ui.debugger.router.DebuggerRoute

/**
 * Save the UI state
 *   - Divider percentages
 *   - Selected VM tab
 *
 * See https://plugins.jetbrains.com/docs/intellij/persisting-state-of-components.html#implementing-the-persistentstatecomponent-interface
 */
interface DebuggerUiSettings {
    val darkTheme: Boolean

    val lastRoute: DebuggerRoute
    val lastViewModelName: String

    val autoselectDebuggerConnections: Boolean

    val alwaysShowCurrentState: Boolean
    val showCurrentRoute: Boolean
    val routerViewModelName: String

    val detailsPanePercentage: Float
}

