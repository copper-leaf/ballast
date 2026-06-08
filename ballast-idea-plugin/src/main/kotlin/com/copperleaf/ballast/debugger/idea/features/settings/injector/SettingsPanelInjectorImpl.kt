package com.copperleaf.ballast.debugger.idea.features.settings.injector

import com.copperleaf.ballast.build
import com.copperleaf.ballast.core.BasicViewModel
import com.copperleaf.ballast.debugger.idea.BallastIntellijPluginInjector
import com.copperleaf.ballast.debugger.idea.features.settings.vm.SettingsUiContract
import com.copperleaf.ballast.debugger.idea.features.settings.vm.SettingsUiEventHandler
import com.copperleaf.ballast.debugger.idea.features.settings.vm.SettingsUiInputHandler
import com.copperleaf.ballast.debugger.idea.features.settings.vm.SettingsUiViewModel
import com.copperleaf.ballast.withViewModel
import com.intellij.openapi.project.Project
import kotlinx.coroutines.CoroutineScope

class SettingsPanelInjectorImpl(
    private val pluginInjector: BallastIntellijPluginInjector,
) : SettingsPanelInjector {
    override val project: Project = pluginInjector.project
    override val settingsPanelCoroutineScope: CoroutineScope = pluginInjector.newMainCoroutineScope()
    override val settingsPanelViewModel: SettingsUiViewModel = BasicViewModel(
        coroutineScope = settingsPanelCoroutineScope,
        config = pluginInjector
            .commonViewModelBuilder(loggingEnabled = false) {
                SettingsUiContract.Inputs.Initialize
            }
            .withViewModel(
                initialState = SettingsUiContract.State(),
                inputHandler = SettingsUiInputHandler(pluginInjector.repository),
                name = "SettingsUi",
            )
            .build(),
        eventHandler = SettingsUiEventHandler(),
    )
}
