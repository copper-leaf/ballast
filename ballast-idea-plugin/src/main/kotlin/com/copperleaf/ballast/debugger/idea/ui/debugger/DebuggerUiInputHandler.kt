package com.copperleaf.ballast.debugger.idea.ui.debugger

import com.copperleaf.ballast.InputHandler
import com.copperleaf.ballast.InputHandlerScope
import com.copperleaf.ballast.debugger.idea.repository.RepositoryViewModel
import com.copperleaf.ballast.debugger.idea.ui.debugger.router.DebuggerRouter
import com.copperleaf.ballast.debugger.idea.ui.debugger.widgets.getRouteForSelectedViewModel
import com.copperleaf.ballast.debugger.server.vm.DebuggerServerContract
import com.copperleaf.ballast.debugger.server.vm.DebuggerServerViewModel
import com.copperleaf.ballast.navigation.routing.RouterContract
import com.copperleaf.ballast.observeFlows
import com.copperleaf.ballast.postInput
import com.copperleaf.ballast.repository.cache.getCachedOrThrow
import kotlinx.coroutines.flow.map

class DebuggerUiInputHandler(
    private val debuggerRouter: DebuggerRouter,
    private val debuggerServerViewModel: DebuggerServerViewModel,
    private val repository: RepositoryViewModel,
) : InputHandler<
        DebuggerUiContract.Inputs,
        DebuggerUiContract.Events,
        DebuggerUiContract.State> {
    override suspend fun InputHandlerScope<
            DebuggerUiContract.Inputs,
            DebuggerUiContract.Events,
            DebuggerUiContract.State>.handleInput(
        input: DebuggerUiContract.Inputs
    ) = when (input) {
        is DebuggerUiContract.Inputs.Initialize -> {
            observeFlows(
                "router state",
                debuggerRouter
                    .observeStates()
                    .map { DebuggerUiContract.Inputs.BackstackChanged(it.backstack) },
                debuggerServerViewModel
                    .observeStates()
                    .map { DebuggerUiContract.Inputs.ServerStateChanged(it.applicationState) },
                repository
                    .observeStates()
                    .map { DebuggerUiContract.Inputs.SettingsChanged(it.settings) },
            )
        }

        is DebuggerUiContract.Inputs.OnConnectionEstablished -> {
            val currentState = getCurrentState()

            if(!currentState.isReady) {
                noOp()
            } else {
                val settingsSnapshot = currentState.settings.getCachedOrThrow()
                if (!settingsSnapshot.autoselectDebuggerConnections) {
                    noOp()
                } else {
                    val latestRoute = settingsSnapshot.lastRoute
                    val latestViewModelName = settingsSnapshot.lastViewModelName
                    logger.debug("Autoselecting route:")
                    logger.debug("    connectionId: ${input.connectionId}")
                    logger.debug("    latestRoute: $latestRoute")
                    logger.debug("    latestViewModelName: $latestViewModelName")

                    val route = if (latestViewModelName.isNotBlank()) {
                        getRouteForSelectedViewModel(
                            settingsSnapshot.lastRoute,
                            input.connectionId,
                            latestViewModelName
                        )
                    } else {
                        getRouteForSelectedViewModel(
                            settingsSnapshot.lastRoute,
                            input.connectionId,
                            null,
                        )
                    }

                    postInput(
                        DebuggerUiContract.Inputs.Navigate(route)
                    )
                }
            }
        }

        is DebuggerUiContract.Inputs.ServerStateChanged -> {
            updateState { it.copy(serverState = input.serverState) }
        }

        is DebuggerUiContract.Inputs.BackstackChanged -> {
            updateState { it.copy(backstack = input.backstack) }
        }

        is DebuggerUiContract.Inputs.SettingsChanged -> {
            val previousState = getCurrentState()
            val currentState = updateStateAndGet { it.copy(settings = input.settings) }

            val requestServerStart = if (currentState.isReady) {
                if(!previousState.isReady) {
                    // server is not yet started, request to start it now
                    true
                } else {
                    // server is already active, restart if the port settings have changed
                    val previousSettingsPort = previousState.settings.getCachedOrThrow().debuggerServerPort
                    val currentSettingsPort = previousState.settings.getCachedOrThrow().debuggerServerPort
                    previousSettingsPort != currentSettingsPort
                }
            } else {
                false
            }

            if(requestServerStart) {
                debuggerServerViewModel.send(DebuggerServerContract.Inputs.StartServer(currentState.settings.getCachedOrThrow()))
            } else {

            }
        }

        is DebuggerUiContract.Inputs.ClearAllConnections -> {
            postEvent(
                DebuggerUiContract.Events.SendCommandToDebuggerServer(
                    DebuggerServerContract.Inputs.ClearAll
                )
            )
        }

        is DebuggerUiContract.Inputs.Navigate -> {
            postEvent(
                DebuggerUiContract.Events.SendCommandToRouter(
                    RouterContract.Inputs.ReplaceTopDestination(input.destinationUrl)
                )
            )
        }

        is DebuggerUiContract.Inputs.UpdateSearchText -> {
            updateState { it.copy(searchText = input.value) }
        }

        is DebuggerUiContract.Inputs.ClearConnection -> {
            noOp()
        }

        is DebuggerUiContract.Inputs.ClearViewModel -> {
            noOp()
        }

        is DebuggerUiContract.Inputs.FocusConnection -> {
            noOp()
        }

        is DebuggerUiContract.Inputs.FocusEvent -> {
            noOp()
        }

        is DebuggerUiContract.Inputs.FocusViewModel -> {
            noOp()
        }

        is DebuggerUiContract.Inputs.SendDebuggerAction -> {
            noOp()
        }

        is DebuggerUiContract.Inputs.UpdateSelectedViewModelContentTab -> {
            noOp()
        }
    }
}
