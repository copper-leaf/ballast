package com.copperleaf.ballast.debugger.idea.features.settings.vm

import com.copperleaf.ballast.InputHandler
import com.copperleaf.ballast.InputHandlerScope
import com.copperleaf.ballast.core.KillSwitch
import com.copperleaf.ballast.debugger.idea.repository.RepositoryContract
import com.copperleaf.ballast.debugger.idea.repository.RepositoryViewModel
import com.copperleaf.ballast.observeFlows
import com.copperleaf.ballast.repository.cache.getCachedOrNull
import kotlinx.coroutines.flow.map

class SettingsUiInputHandler(
    private val repository: RepositoryViewModel,
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
        is SettingsUiContract.Inputs.Initialize -> {
            observeFlows(
                "Initialize",
                repository
                    .observeStates()
                    .map { SettingsUiContract.Inputs.SavedSettingsUpdated(it.settings) }
            )
        }

        is SettingsUiContract.Inputs.SavedSettingsUpdated -> {
            val newSettings = input.cachedSettings.getCachedOrNull()
            if(newSettings != null) {
                updateState {
                    it.copy(
                        cachedSettings = input.cachedSettings,
                        originalSettings = newSettings,
                        modifiedSettings = newSettings,
                    )
                }
            } else {
                updateState {
                    it.copy(
                        cachedSettings = input.cachedSettings,
                    )
                }
            }
        }

        is SettingsUiContract.Inputs.UpdateSettings -> {
            updateState { it.copy(modifiedSettings = input.value(it.modifiedSettings)) }
        }

        is SettingsUiContract.Inputs.DiscardChanges -> {
            updateState { it.copy(modifiedSettings = it.originalSettings) }
        }

        is SettingsUiContract.Inputs.RestoreDefaultSettings -> {
            updateState { it.copy(modifiedSettings = it.defaultValues) }
        }

        is SettingsUiContract.Inputs.ApplySettings -> {
            val previousState = getCurrentState()

            sideJob("ApplySettings") {
                repository.send(RepositoryContract.Inputs.SaveUpdatedSettings(previousState.modifiedSettings))
            }
        }

        is SettingsUiContract.Inputs.CloseGracefully -> {
            sideJob("CloseGracefully") {
                getInterceptor(KillSwitch.Key).requestGracefulShutdown()
            }
        }
    }
}
