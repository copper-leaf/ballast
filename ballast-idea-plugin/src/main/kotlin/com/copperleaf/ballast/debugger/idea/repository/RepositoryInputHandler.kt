package com.copperleaf.ballast.debugger.idea.repository

import com.copperleaf.ballast.InputHandler
import com.copperleaf.ballast.InputHandlerScope
import com.copperleaf.ballast.debugger.idea.settings.IntellijPluginSettingsSnapshot
import com.copperleaf.ballast.repository.cache.Cached

class RepositoryInputHandler : InputHandler<
        RepositoryContract.Inputs,
        RepositoryContract.Events,
        RepositoryContract.State> {
    override suspend fun InputHandlerScope<
            RepositoryContract.Inputs,
            RepositoryContract.Events,
            RepositoryContract.State>.handleInput(
        input: RepositoryContract.Inputs
    ) = when (input) {
        is RepositoryContract.Inputs.Initialize -> {
            updateSavedSettingsInState()
        }

        is RepositoryContract.Inputs.SaveUpdatedSettings -> {
            // save the updated values
            val snapshot = input.settings
            getCurrentState().persistentSettings.applyFromSnapshot(snapshot)

            // capture a snapshot and set it in the Repository's state
            updateSavedSettingsInState()
        }
    }

    private suspend fun InputHandlerScope<
            RepositoryContract.Inputs,
            RepositoryContract.Events,
            RepositoryContract.State>.updateSavedSettingsInState() {
        val persistentSettings = getCurrentState().persistentSettings

        updateState {
            it.copy(
                settings = Cached.Value(
                    IntellijPluginSettingsSnapshot.fromSettings(persistentSettings)
                )
            )
        }
    }
}
