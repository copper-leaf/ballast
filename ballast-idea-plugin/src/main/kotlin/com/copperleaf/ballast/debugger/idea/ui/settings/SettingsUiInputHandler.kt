package com.copperleaf.ballast.debugger.idea.ui.settings

import com.copperleaf.ballast.InputHandler
import com.copperleaf.ballast.InputHandlerScope
import com.copperleaf.ballast.debugger.idea.settings.BallastIntellijPluginPersistentSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel

class SettingsUiInputHandler(
    private val settings: BallastIntellijPluginPersistentSettings,
    private val settingsPanelCoroutineScope: CoroutineScope,
) : InputHandler<
        SettingsUiContract.Inputs,
        SettingsUiContract.Events,
        SettingsUiContract.State> {
    override suspend fun InputHandlerScope<
            SettingsUiContract.Inputs,
            SettingsUiContract.Events,
            SettingsUiContract.State>.handleInput(
        input: SettingsUiContract.Inputs
    ) = when (input) {
        is SettingsUiContract.Inputs.UpdateSettings -> {
            updateState { it.copy(modifiedSettings = input.value(it.modifiedSettings)) }
        }

        is SettingsUiContract.Inputs.ResetSettings -> {
            updateState { it.copy(modifiedSettings = it.originalSettings) }
        }

        is SettingsUiContract.Inputs.RestoreDefaultSettings -> {
            updateState { it.copy(modifiedSettings = it.defaultValues) }
        }

        is SettingsUiContract.Inputs.ApplySettings -> {
            val previousState = getAndUpdateState {
                it.copy(
                    originalSettings = it.modifiedSettings,
                    modifiedSettings = it.modifiedSettings,
                )
            }

            settings.updateFromSnapshot(previousState.modifiedSettings)
        }

        is SettingsUiContract.Inputs.CloseGracefully -> {
            sideJob("CloseGracefully") { settingsPanelCoroutineScope.cancel() }
        }
    }
}
