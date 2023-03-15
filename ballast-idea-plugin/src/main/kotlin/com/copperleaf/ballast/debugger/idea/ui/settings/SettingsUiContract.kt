package com.copperleaf.ballast.debugger.idea.ui.settings

import com.copperleaf.ballast.debugger.idea.settings.IntellijPluginSettingsSnapshot
import com.copperleaf.ballast.repository.cache.Cached

object SettingsUiContract {
    data class State(
        val cachedSettings: Cached<IntellijPluginSettingsSnapshot> = Cached.NotLoaded(),
        val defaultValues: IntellijPluginSettingsSnapshot = IntellijPluginSettingsSnapshot.defaults(),
        val originalSettings: IntellijPluginSettingsSnapshot = IntellijPluginSettingsSnapshot.defaults(),
        val modifiedSettings: IntellijPluginSettingsSnapshot = originalSettings,
    ) {
        val isModified: Boolean = modifiedSettings != originalSettings
    }

    sealed class Inputs {
        object Initialize : Inputs()

        data class SavedSettingsUpdated(val cachedSettings: Cached<IntellijPluginSettingsSnapshot>) : Inputs()
        data class UpdateSettings(val value: IntellijPluginSettingsSnapshot.()->IntellijPluginSettingsSnapshot) : Inputs()
        object DiscardChanges : Inputs()
        object RestoreDefaultSettings : Inputs()
        object ApplySettings : Inputs()
        object CloseGracefully : Inputs()
    }

    sealed class Events {
    }
}
