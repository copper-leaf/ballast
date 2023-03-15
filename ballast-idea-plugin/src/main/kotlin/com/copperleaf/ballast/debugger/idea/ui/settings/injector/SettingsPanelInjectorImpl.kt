package com.copperleaf.ballast.debugger.idea.ui.settings.injector

import com.copperleaf.ballast.build
import com.copperleaf.ballast.core.BasicViewModel
import com.copperleaf.ballast.core.KillSwitch
import com.copperleaf.ballast.debugger.idea.BallastIntellijPluginInjector
import com.copperleaf.ballast.debugger.idea.ui.settings.vm.SettingsUiContract
import com.copperleaf.ballast.debugger.idea.ui.settings.vm.SettingsUiEventHandler
import com.copperleaf.ballast.debugger.idea.ui.settings.vm.SettingsUiInputHandler
import com.copperleaf.ballast.debugger.idea.ui.settings.vm.SettingsUiViewModel
import com.copperleaf.ballast.plusAssign
import com.copperleaf.ballast.withViewModel
import com.intellij.openapi.project.Project
import kotlinx.coroutines.CoroutineScope

class SettingsPanelInjectorImpl(
    private val pluginInjector: BallastIntellijPluginInjector,
) : SettingsPanelInjector {
    override val project: Project = pluginInjector.project
    override val settingsPanelCoroutineScope: CoroutineScope = pluginInjector.newMainCoroutineScope()
    private val settingsPanelKillSwitch: KillSwitch<
            SettingsUiContract.Inputs,
            SettingsUiContract.Events,
            SettingsUiContract.State> = KillSwitch()

    override val settingsPanelViewModel: SettingsUiViewModel = BasicViewModel(
        coroutineScope = settingsPanelCoroutineScope,
        config = pluginInjector
            .commonViewModelBuilder(loggingEnabled = false) {
                SettingsUiContract.Inputs.Initialize
            }
            .apply { this += settingsPanelKillSwitch }
            .withViewModel(
                initialState = SettingsUiContract.State(),
                inputHandler = SettingsUiInputHandler(pluginInjector.repository),
                name = "SettingsUi",
            )
            .build(),
        eventHandler = SettingsUiEventHandler(),
    )
}
