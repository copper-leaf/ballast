package com.copperleaf.ballast.debugger.idea.repository

import com.copperleaf.ballast.InputHandler
import com.copperleaf.ballast.InputHandlerScope
import com.copperleaf.ballast.debugger.idea.settings.IntellijPluginSettingsSnapshot
import com.copperleaf.ballast.postInput
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

        is RepositoryContract.Inputs.SavedSettingsUpdated -> {
            updateState { it.copy(settings = input.settings) }
        }

        is RepositoryContract.Inputs.SaveUpdatedSettings -> {
            // save the updated values
            val snapshot = input.settings
            getCurrentState().persistentSettings.apply {
                this.darkTheme = snapshot.darkTheme

                this.lastRoute = snapshot.lastRoute
                this.lastViewModelName = snapshot.lastViewModelName

                this.debuggerServerPort = snapshot.debuggerServerPort
                this.autoselectDebuggerConnections = snapshot.autoselectDebuggerConnections

                this.alwaysShowCurrentState = snapshot.alwaysShowCurrentState
                this.showCurrentRoute = snapshot.showCurrentRoute
                this.routerViewModelName = snapshot.routerViewModelName

                this.detailsPanePercentage = snapshot.detailsPanePercentage
            }

            // capture a snapshot and set it in the Repository's state
            updateSavedSettingsInState()
        }
    }

    private suspend fun InputHandlerScope<
            RepositoryContract.Inputs,
            RepositoryContract.Events,
            RepositoryContract.State>.updateSavedSettingsInState() {
        postInput(
            RepositoryContract.Inputs.SavedSettingsUpdated(
                Cached.Value(
                    with(getCurrentState().persistentSettings) {
                        IntellijPluginSettingsSnapshot(
                            darkTheme = this.darkTheme,
                            debuggerServerPort = this.debuggerServerPort,
                            lastRoute = this.lastRoute,
                            lastViewModelName = this.lastViewModelName,
                            autoselectDebuggerConnections = this.autoselectDebuggerConnections,
                            alwaysShowCurrentState = this.alwaysShowCurrentState,
                            showCurrentRoute = this.showCurrentRoute,
                            routerViewModelName = this.routerViewModelName,
                            detailsPanePercentage = this.detailsPanePercentage,
                        )
                    }
                )
            )
        )
    }
}
