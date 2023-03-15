package com.copperleaf.ballast.debugger.idea.ui.settings.injector

import com.copperleaf.ballast.debugger.idea.BallastIntellijPluginInjector
import com.copperleaf.ballast.debugger.idea.ui.settings.vm.SettingsUiViewModel
import com.intellij.openapi.project.Project
import kotlinx.coroutines.CoroutineScope

interface SettingsPanelInjector {
    val project: Project
    val settingsPanelCoroutineScope: CoroutineScope
    val settingsPanelViewModel: SettingsUiViewModel

    companion object {
        fun getInstance(
            project: Project,
        ): SettingsPanelInjector {
            return SettingsPanelInjectorImpl(
                BallastIntellijPluginInjector.getInstance(project),
            )
        }
    }
}
