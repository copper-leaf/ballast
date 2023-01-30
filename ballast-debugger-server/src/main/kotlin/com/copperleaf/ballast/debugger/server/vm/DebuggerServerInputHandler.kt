package com.copperleaf.ballast.debugger.server.vm

import com.copperleaf.ballast.InputHandler
import com.copperleaf.ballast.InputHandlerScope
import com.copperleaf.ballast.debugger.models.BallastConnectionState
import com.copperleaf.ballast.debugger.models.BallastDebuggerEvent
import com.copperleaf.ballast.debugger.models.BallastViewModelState
import com.copperleaf.ballast.debugger.models.removeConnection
import com.copperleaf.ballast.debugger.models.updateConnection
import com.copperleaf.ballast.debugger.models.updateViewModel
import com.copperleaf.ballast.debugger.models.updateWithDebuggerEvent
import com.copperleaf.ballast.debugger.server.BallastDebuggerServerConnection

class DebuggerServerInputHandler : InputHandler<
        DebuggerServerContract.Inputs,
        DebuggerServerContract.Events,
        DebuggerServerContract.State> {
    override suspend fun InputHandlerScope<
            DebuggerServerContract.Inputs,
            DebuggerServerContract.Events,
            DebuggerServerContract.State>.handleInput(
        input: DebuggerServerContract.Inputs
    ) = when (input) {
        is DebuggerServerContract.Inputs.StartServer -> {
            val currentState = getCurrentState()
            sideJob("Websocket Server") {
                val server = BallastDebuggerServerConnection(
                    settings = input.settings,
                    outgoingActions = currentState.actions,
                    postInput = { postInput(it) }
                )

                server.runServer()
            }
        }

        is DebuggerServerContract.Inputs.ConnectionEstablished -> {
            updateState {
                it.copy(
                    applicationState = it.applicationState.updateConnection(input.connectionId) {
                        copy(connectionBallastVersion = input.connectionBallastVersion)
                    },
                )
            }
            postEvent(
                DebuggerServerContract.Events.ConnectionEstablished(input.connectionId)
            )
        }


        is DebuggerServerContract.Inputs.ClearAll -> {
            updateState { DebuggerServerContract.State(actions = it.actions) }
        }

        is DebuggerServerContract.Inputs.ClearConnection -> {
            updateState {
                it.copy(
                    applicationState = it.applicationState.updateConnection(input.connectionId) {
                        BallastConnectionState(input.connectionId, this.connectionBallastVersion)
                    }
                )
            }
        }

        is DebuggerServerContract.Inputs.RemoveConnection -> {
            updateState {
                it.copy(
                    applicationState = it.applicationState.removeConnection(input.connectionId)
                )
            }
        }

        is DebuggerServerContract.Inputs.ClearViewModel -> {
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

        is DebuggerServerContract.Inputs.DebuggerEventReceived -> {
            updateState {
                it.copy(
                    allMessages = it.allMessages + input.message,
                    applicationState = it.applicationState.updateConnection(input.message.connectionId) {

                        if (input.message is BallastDebuggerEvent.Heartbeat) {
                            copy(connectionBallastVersion = input.message.connectionBallastVersion)
                        } else {
                            updateViewModel(input.message.viewModelName) {
                                // on the server, we do not have the actual values, since we do not assume them to be
                                // serializable and sent to the server. Only the text is actually sent
                                updateWithDebuggerEvent(input.message, null)
                            }
                        }

                    }
                )
            }
        }

        is DebuggerServerContract.Inputs.SendDebuggerAction -> {
            val currentState = getCurrentState()

            currentState.actions.emit(input.action)
        }
    }
}
