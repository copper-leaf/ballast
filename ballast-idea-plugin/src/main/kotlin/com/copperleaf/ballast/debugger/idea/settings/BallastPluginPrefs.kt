package com.copperleaf.ballast.debugger.idea.settings

/**
 * Save the UI state
 *   - Divider percentages
 *   - Selected VM tab
 *
 * See https://plugins.jetbrains.com/docs/intellij/persisting-state-of-components.html#implementing-the-persistentstatecomponent-interface
 */
interface BallastPluginPrefs {
    var debuggerPort: Int

    var connectionsPanePercentage: Float
    var viewModelsPanePercentage: Float
    var eventsPanePercentage: Float
}

