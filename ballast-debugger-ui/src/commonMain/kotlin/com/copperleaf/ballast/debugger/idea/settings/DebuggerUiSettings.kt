package com.copperleaf.ballast.debugger.idea.settings

import com.copperleaf.ballast.debugger.idea.features.debugger.router.DebuggerRoute

/**
 * Save the UI state
 *   - Divider percentages
 *   - Selected VM tab
 *
 * See https://plugins.jetbrains.com/docs/intellij/persisting-state-of-components.html#implementing-the-persistentstatecomponent-interface
 */
public interface DebuggerUiSettings {
    public val lastRoute: DebuggerRoute
    public val lastViewModelName: String
    public val autoselectDebuggerConnections: Boolean
    public val alwaysShowCurrentState: Boolean
    public val showCurrentRoute: Boolean
    public val routerViewModelName: String
    public val detailsPanePercentage: Float
}
