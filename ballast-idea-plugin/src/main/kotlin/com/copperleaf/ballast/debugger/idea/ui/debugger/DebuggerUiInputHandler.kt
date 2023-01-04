package com.copperleaf.ballast.debugger.idea.ui.debugger

import com.copperleaf.ballast.InputHandler
import com.copperleaf.ballast.InputHandlerScope
import com.copperleaf.ballast.debugger.idea.ui.debugger.router.DebuggerRoute
import com.copperleaf.ballast.debugger.server.vm.DebuggerServerContract
import com.copperleaf.ballast.navigation.routing.RouterContract
import com.copperleaf.ballast.observeFlows
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map

class DebuggerUiInputHandler(
    private val serverStateFlow: StateFlow<DebuggerServerContract.State>,
    private val routerStateFlow: StateFlow<RouterContract.State<DebuggerRoute>>,
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
                serverStateFlow.map { DebuggerUiContract.Inputs.ServerStateChanged(it.applicationState) },
                routerStateFlow.map { DebuggerUiContract.Inputs.BackstackChanged(it.backstack) },
            )
        }

        is DebuggerUiContract.Inputs.ServerStateChanged -> {
            updateState { it.copy(serverState = input.serverState) }
        }

        is DebuggerUiContract.Inputs.BackstackChanged -> {
            updateState { it.copy(backstack = input.backstack) }
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
