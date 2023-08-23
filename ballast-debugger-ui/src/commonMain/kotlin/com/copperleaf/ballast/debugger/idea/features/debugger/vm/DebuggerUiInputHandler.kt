package com.copperleaf.ballast.debugger.idea.features.debugger.vm

import com.copperleaf.ballast.InputHandler
import com.copperleaf.ballast.InputHandlerScope
import com.copperleaf.ballast.debugger.idea.features.debugger.repository.DebuggerUseCase
import com.copperleaf.ballast.debugger.idea.features.debugger.router.DebuggerRouter
import com.copperleaf.ballast.debugger.idea.features.debugger.ui.widgets.getRouteForSelectedViewModel
import com.copperleaf.ballast.debugger.server.vm.DebuggerServerContract
import com.copperleaf.ballast.debugger.server.vm.DebuggerServerViewModel
import com.copperleaf.ballast.navigation.routing.RouterContract
import com.copperleaf.ballast.observeFlows
import com.copperleaf.ballast.postInput
import kotlinx.coroutines.flow.map

public class DebuggerUiInputHandler(
    private val debuggerRouter: DebuggerRouter,
    private val debuggerServerViewModel: DebuggerServerViewModel,
    private val useCase: DebuggerUseCase,
) : InputHandler<
        DebuggerUiContract.Inputs,
        DebuggerUiContract.Events,
        DebuggerUiContract.State> {
    override suspend fun InputHandlerScope<
            DebuggerUiContract.Inputs,
            DebuggerUiContract.Events,
            DebuggerUiContract.State>.handleInput(
        input: DebuggerUiContract.Inputs
    ): Unit = when (input) {
        is DebuggerUiContract.Inputs.Initialize -> {
            observeFlows(
                "router state",
                debuggerRouter
                    .observeStates()
                    .map { DebuggerUiContract.Inputs.BackstackChanged(it.backstack) },
                debuggerServerViewModel
                    .observeStates()
                    .map { DebuggerUiContract.Inputs.ServerStateChanged(it.applicationState) },
                useCase
                    .observeGeneralSettings()
                    .map { DebuggerUiContract.Inputs.GeneralSettingsChanged(it) },
                useCase
                    .observeBallastDebuggerServerSettings()
                    .map { DebuggerUiContract.Inputs.BallastDebuggerServerSettingsChanged(it) },
                useCase
                    .observeDebuggerUiSettings()
                    .map { DebuggerUiContract.Inputs.DebuggerUiSettingsChanged(it) },
            )
        }

        is DebuggerUiContract.Inputs.OnConnectionEstablished -> {
            val currentState = getCurrentState()

            if (!currentState.isReady) {
                noOp()
            } else {
                if (!currentState.debuggerUiSettings.autoselectDebuggerConnections) {
                    noOp()
                } else {
                    val latestRoute = currentState.debuggerUiSettings.lastRoute
                    val latestViewModelName = currentState.debuggerUiSettings.lastViewModelName

                    val route = if (latestViewModelName.isNotBlank()) {
                        getRouteForSelectedViewModel(
                            latestRoute,
                            input.connectionId,
                            latestViewModelName
                        )
                    } else {
                        getRouteForSelectedViewModel(
                            latestRoute,
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

        is DebuggerUiContract.Inputs.GeneralSettingsChanged -> {
            val previousState = getCurrentState()
            val currentState = updateStateAndGet { it.copy(cachedGeneralSettings = input.settings) }
            startServerIfNeeded(previousState, currentState)
        }

        is DebuggerUiContract.Inputs.BallastDebuggerServerSettingsChanged -> {
            val previousState = getCurrentState()
            val currentState = updateStateAndGet { it.copy(cachedBallastDebuggerServerSettings = input.settings) }
            startServerIfNeeded(previousState, currentState)
        }

        is DebuggerUiContract.Inputs.DebuggerUiSettingsChanged -> {
            val previousState = getCurrentState()
            val currentState = updateStateAndGet { it.copy(cachedDebuggerUiSettings = input.settings) }
            startServerIfNeeded(previousState, currentState)
        }

        is DebuggerUiContract.Inputs.Navigate -> {
            sideJob("Navigate") {
                debuggerRouter.send(
                    RouterContract.Inputs.ReplaceTopDestination(input.destinationUrl)
                )
            }
        }

        is DebuggerUiContract.Inputs.UpdateSearchText -> {
            updateState { it.copy(searchText = input.value) }
        }

        is DebuggerUiContract.Inputs.SendToDebuggerServer -> {
            sideJob("SendToDebuggerServer") {
                debuggerServerViewModel.send(input.debuggerServerInput)
            }
        }

        is DebuggerUiContract.Inputs.CopyToClipboard -> {
            postEvent(DebuggerUiContract.Events.CopyToClipboard(input.text))
        }
    }

    private suspend fun InputHandlerScope<
            DebuggerUiContract.Inputs,
            DebuggerUiContract.Events,
            DebuggerUiContract.State>.startServerIfNeeded(
        previousState: DebuggerUiContract.State,
        currentState: DebuggerUiContract.State,
    ) {
        val requestServerStart = if (currentState.isReady) {
            if (!previousState.isReady) {
                // server is not yet started, request to start it now
                true
            } else {
                // server is already active, restart if the port settings have changed
                val previousSettingsPort = previousState.ballastDebuggerServerSettings.debuggerServerPort
                val currentSettingsPort = currentState.ballastDebuggerServerSettings.debuggerServerPort
                previousSettingsPort != currentSettingsPort
            }
        } else {
            false
        }

        if (requestServerStart) {
            sideJob("start server") {
                debuggerServerViewModel.send(
                    DebuggerServerContract.Inputs.StartServer(currentState.ballastDebuggerServerSettings)
                )
            }
        }
    }
}
