package com.copperleaf.ballast.debugger.ui.debugger

import androidx.compose.runtime.snapshotFlow
import com.copperleaf.ballast.InputHandler
import com.copperleaf.ballast.InputHandlerScope
import com.copperleaf.ballast.debugger.models.BallastConnectionState
import com.copperleaf.ballast.debugger.models.BallastDebuggerEvent
import com.copperleaf.ballast.debugger.models.BallastViewModelState
import com.copperleaf.ballast.debugger.models.updateConnection
import com.copperleaf.ballast.debugger.models.updateViewModel
import com.copperleaf.ballast.debugger.models.updateWithDebuggerEvent
import com.copperleaf.ballast.debugger.server.BallastDebuggerServerConnection
import com.copperleaf.ballast.observeFlows
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

class DebuggerInputHandler : InputHandler<
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
            sideJob("Websocket Server") {
                val server = BallastDebuggerServerConnection(
                    port = input.port,
                    outgoingActions = currentStateWhenStarted.actions,
                    postInput = { postInput(it) }
                )

                server.runServer()
            }

            observeFlows("SplitPane State Observer") {
                listOf(
                    snapshotFlow { currentStateWhenStarted.connectionsPanePercentage.positionPercentage }
                        .distinctUntilChanged()
                        .map { DebuggerContract.Inputs.UpdateConnectionsPanePercentageValue(it) },

                    snapshotFlow { currentStateWhenStarted.viewModelsPanePercentage.positionPercentage }
                        .distinctUntilChanged()
                        .map { DebuggerContract.Inputs.UpdateViewModelsPanePercentageValue(it) },

                    snapshotFlow { currentStateWhenStarted.eventsPanePercentage.positionPercentage }
                        .distinctUntilChanged()
                        .map { DebuggerContract.Inputs.UpdateEventsPanePercentageValue(it) }

                )
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
        is DebuggerContract.Inputs.SendDebuggerAction -> {
            val currentState = getCurrentState()

            currentState.actions.emit(input.action)
        }

        is DebuggerContract.Inputs.UpdateSelectedViewModelContentTab -> {
            updateState { it.copy(selectedViewModelContentTab = input.value) }
        }
        is DebuggerContract.Inputs.UpdateConnectionsPanePercentageValue -> {
            updateState { it.copy(connectionsPanePercentageValue = input.value) }
        }
        is DebuggerContract.Inputs.UpdateEventsPanePercentageValue -> {
            updateState { it.copy(eventsPanePercentageValue = input.value) }
        }
        is DebuggerContract.Inputs.UpdateViewModelsPanePercentageValue -> {
            updateState { it.copy(viewModelsPanePercentageValue = input.value) }
        }
    }
}
