package com.copperleaf.ballast.debugger.windows.debugger

import com.copperleaf.ballast.InputHandler
import com.copperleaf.ballast.InputHandlerScope
import com.copperleaf.ballast.debugger.models.BallastConnectionState
import com.copperleaf.ballast.debugger.models.BallastViewModelState
import com.copperleaf.ballast.debugger.models.updateConnection
import com.copperleaf.ballast.debugger.models.updateViewModel
import com.copperleaf.ballast.debugger.models.updateWithDebuggerEvent
import com.copperleaf.ballast.debugger.server.BallastDebuggerServerConnection
import org.slf4j.Logger

class DebuggerInputHandler(
    private val logger: Logger,
) : InputHandler<
        DebuggerContract.Inputs,
        DebuggerContract.Events,
        DebuggerContract.State> {
    override suspend fun InputHandlerScope<
        DebuggerContract.Inputs,
        DebuggerContract.Events,
        DebuggerContract.State>.handleInput(
        input: DebuggerContract.Inputs
    ) = when (input) {
        is DebuggerContract.Inputs.StartServer -> {
            sideEffect("Websocket Server") {
                val server = BallastDebuggerServerConnection(
                    port = input.port,
                    outgoingActions = currentStateWhenStarted.actions,
                    postInput = { postInput(it) }
                )

                server.runServer()
            }
        }

        is DebuggerContract.Inputs.ConnectionEstablished -> {
            updateState {
                it.copy(
                    applicationState = it.applicationState.updateConnection(input.connectionId) {
                        copy(connectionBallastVersion = input.connectionBallastVersion)
                    }
                )
            }
        }

        is DebuggerContract.Inputs.FocusConnection -> {
            updateState {
                it.copy(
                    focusedConnectionId = input.connectionId,
                    focusedViewModelName = null,
                    focusedDebuggerEventUuid = null,
                )
            }
        }
        is DebuggerContract.Inputs.FocusViewModel -> {
            updateState {
                it.copy(
                    focusedConnectionId = input.connectionId,
                    focusedViewModelName = input.viewModelName,
                    focusedDebuggerEventUuid = null,
                )
            }
        }
        is DebuggerContract.Inputs.FocusEvent -> {
            updateState {
                it.copy(
                    focusedConnectionId = input.connectionId,
                    focusedViewModelName = input.viewModelName,
                    focusedDebuggerEventUuid = input.eventUuid,
                )
            }
        }

        is DebuggerContract.Inputs.ClearAll -> {
            updateState { DebuggerContract.State(actions = it.actions) }
        }
        is DebuggerContract.Inputs.ClearConnection -> {
            updateState {
                it.copy(
                    applicationState = it.applicationState.updateConnection(input.connectionId) {
                        BallastConnectionState(input.connectionId, this.connectionBallastVersion)
                    }
                )
            }
        }
        is DebuggerContract.Inputs.ClearViewModel -> {
            updateState {
                it.copy(
                    applicationState = it.applicationState.updateConnection(input.connectionId) {
                        updateViewModel(input.viewModelName) {
                            BallastViewModelState(input.connectionId, input.viewModelName)
                        }
                    }
                )
            }
        }

        is DebuggerContract.Inputs.DebuggerEventReceived -> {
            updateState {
                it.copy(
                    allMessages = it.allMessages + input.message,
                    applicationState = it.applicationState.updateConnection(input.message.connectionId) {
                        updateViewModel(input.message.viewModelName) {
                            updateWithDebuggerEvent(input.message)
                        }
                    }
                )
            }
        }
        is DebuggerContract.Inputs.SendDebuggerAction -> {
            val currentState = getCurrentState()

            currentState.actions.emit(input.action)
        }
    }
}
