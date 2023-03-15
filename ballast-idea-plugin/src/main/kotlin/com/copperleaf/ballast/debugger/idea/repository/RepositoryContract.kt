package com.copperleaf.ballast.debugger.idea.repository

import com.copperleaf.ballast.debugger.idea.settings.IntellijPluginPersistentSettings
import com.copperleaf.ballast.debugger.idea.settings.IntellijPluginSettingsSnapshot
import com.copperleaf.ballast.repository.cache.Cached

object RepositoryContract {
    data class State(
        internal val persistentSettings: IntellijPluginPersistentSettings = IntellijPluginPersistentSettings(),
        val settings: Cached<IntellijPluginSettingsSnapshot> = Cached.NotLoaded(),
    )

    sealed class Inputs {
        object Initialize : Inputs()

        data class SaveUpdatedSettings(val settings: IntellijPluginSettingsSnapshot) : Inputs()
    }

    sealed class Events
}
