package com.copperleaf.ballast.debugger.idea

import com.copperleaf.ballast.debugger.idea.settings.IntellijPluginSettingsSnapshot
import com.intellij.openapi.project.Project

class BallastIdeaPlugin {
    companion object {
        fun getSettings(project: Project): IntellijPluginSettingsSnapshot {
            val injector = BallastIntellijPluginInjector.getInstance(project)
            val persistentSettings = injector.repository.observeStates().value.persistentSettings
            val settingsSnapshot = IntellijPluginSettingsSnapshot.fromSettings(persistentSettings)

            return settingsSnapshot
        }
    }
}
