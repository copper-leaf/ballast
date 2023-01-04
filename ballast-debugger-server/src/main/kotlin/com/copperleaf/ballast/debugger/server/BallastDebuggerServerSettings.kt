package com.copperleaf.ballast.debugger.server

/**
 * Save the UI state
 *   - Divider percentages
 *   - Selected VM tab
 *
 * See https://plugins.jetbrains.com/docs/intellij/persisting-state-of-components.html#implementing-the-persistentstatecomponent-interface
 */
interface BallastDebuggerServerSettings {
    val debuggerServerPort: Int
}

