package com.copperleaf.ballast.debugger.idea.settings

import com.copperleaf.ballast.debugger.idea.ui.debugger.BallastDebuggerUiSettings
import com.copperleaf.ballast.debugger.server.BallastDebuggerServerSettings

/**
 * All settings for the plugin, wrapped into a single interface.
 *
 * See [BallastIntellijPluginMutableSettings] for a mutable variant of settings.
 */
interface BallastIntellijPluginSettings : BallastDebuggerUiSettings, BallastDebuggerServerSettings
