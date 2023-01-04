package com.copperleaf.ballast.debugger.idea.ui.settings

import com.copperleaf.ballast.debugger.idea.settings.BallastIntellijPluginSettingsSnapshot

object SettingsUiContract {
    data class State(
        val defaultValues: BallastIntellijPluginSettingsSnapshot,
        val originalSettings: BallastIntellijPluginSettingsSnapshot,
        val modifiedSettings: BallastIntellijPluginSettingsSnapshot = originalSettings,
    ) {
        val isModified: Boolean = modifiedSettings != originalSettings
    }

    sealed class Inputs {
        data class UpdateSettings(val value: BallastIntellijPluginSettingsSnapshot.()->BallastIntellijPluginSettingsSnapshot) : Inputs()
        object ResetSettings : Inputs()
        object RestoreDefaultSettings : Inputs()
        object ApplySettings : Inputs()
        object CloseGracefully : Inputs()
    }

    sealed class Events {
    }
}
