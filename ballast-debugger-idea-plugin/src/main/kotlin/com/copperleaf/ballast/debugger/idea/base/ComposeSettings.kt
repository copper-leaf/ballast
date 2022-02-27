package com.copperleaf.ballast.debugger.idea.base

import androidx.compose.runtime.Composable
import com.intellij.openapi.options.Configurable

/**
 * Allow the user to configure the port of the debugger server
 *
 * See https://plugins.jetbrains.com/docs/intellij/settings-guide.html#the-configurable-interface
 */
abstract class ComposeSettings : Configurable {

    @Composable
    abstract fun Content()
}
