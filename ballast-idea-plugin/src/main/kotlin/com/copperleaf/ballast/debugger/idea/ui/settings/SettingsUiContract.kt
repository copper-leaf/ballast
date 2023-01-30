package com.copperleaf.ballast.debugger.idea.ui.settings

import com.copperleaf.ballast.debugger.idea.settings.IntellijPluginSettingsSnapshot

object SettingsUiContract {
    data class State(
        val defaultValues: IntellijPluginSettingsSnapshot,
        val originalSettings: IntellijPluginSettingsSnapshot,
        val modifiedSettings: IntellijPluginSettingsSnapshot = originalSettings,
    ) {
        val isModified: Boolean = modifiedSettings != originalSettings
    }

    sealed class Inputs {
        data class UpdateSettings(val value: IntellijPluginSettingsSnapshot.()->IntellijPluginSettingsSnapshot) : Inputs()
        object DiscardChanges : Inputs()
        object RestoreDefaultSettings : Inputs()
        object ApplySettings : Inputs()
        object CloseGracefully : Inputs()
    }

    sealed class Events {
    }
}
