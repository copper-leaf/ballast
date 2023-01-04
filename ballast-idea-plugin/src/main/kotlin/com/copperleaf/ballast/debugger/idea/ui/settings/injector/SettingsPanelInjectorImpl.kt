package com.copperleaf.ballast.debugger.idea.ui.settings.injector

import com.copperleaf.ballast.build
import com.copperleaf.ballast.core.BasicViewModel
import com.copperleaf.ballast.debugger.idea.BallastIntellijPluginInjector
import com.copperleaf.ballast.debugger.idea.ui.settings.SettingsUiContract
import com.copperleaf.ballast.debugger.idea.ui.settings.SettingsUiEventHandler
import com.copperleaf.ballast.debugger.idea.ui.settings.SettingsUiInputHandler
import com.copperleaf.ballast.debugger.idea.ui.settings.SettingsUiViewModel
import com.copperleaf.ballast.withViewModel
import kotlinx.coroutines.CoroutineScope

class SettingsPanelInjectorImpl(
    private val pluginInjector: BallastIntellijPluginInjector,
) : SettingsPanelInjector {
    override val settingsPanelCoroutineScope: CoroutineScope = pluginInjector.newMainCoroutineScope()

    override val settingsPanelViewModel: SettingsUiViewModel = BasicViewModel(
        config = pluginInjector
            .commonViewModelBuilder()
            .withViewModel(
                initialState = SettingsUiContract.State(
                    defaultValues = pluginInjector.settings.defaults(),
                    originalSettings = pluginInjector.settings.snapshot(),
                ),
                inputHandler = SettingsUiInputHandler(pluginInjector.settings, settingsPanelCoroutineScope),
                name = "SettingsUi",
            )
            .build(),
        eventHandler = SettingsUiEventHandler(),
        coroutineScope = settingsPanelCoroutineScope,
    )
}
