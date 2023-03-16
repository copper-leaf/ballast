package com.copperleaf.ballast.debugger.idea.settings

/**
 * General settings for the whole plugin
 *
 * See https://plugins.jetbrains.com/docs/intellij/persisting-state-of-components.html#implementing-the-persistentstatecomponent-interface
 */
interface GeneralSettings {
    val ballastVersion: String
    val darkTheme: Boolean
}

