@file:Suppress("UNUSED_PARAMETER")
package com.copperleaf.ballast.debugger.idea.ui.debugger.widgets

import androidx.compose.foundation.ContextMenuArea
import androidx.compose.foundation.ContextMenuItem
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.items
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.ListItem
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.copperleaf.ballast.debugger.idea.ui.debugger.DebuggerUiContract
import com.copperleaf.ballast.debugger.models.BallastConnectionState
import com.copperleaf.ballast.debugger.models.BallastDebuggerAction
import com.copperleaf.ballast.debugger.models.BallastInputState
import com.copperleaf.ballast.debugger.models.BallastViewModelState
import com.copperleaf.ballast.debugger.utils.minus
import com.copperleaf.ballast.debugger.utils.removeFraction
import org.jetbrains.compose.splitpane.rememberSplitPaneState
import kotlin.time.Duration
import kotlin.time.DurationUnit

@Composable
fun InputList(
    uiState: DebuggerUiContract.State,
    viewModelState: BallastViewModelState,
    postInput: (DebuggerUiContract.Inputs) -> Unit,
) {
    SplitPane(
        splitPaneState = uiState.eventsPanePercentage,
        navigation = {
            items(viewModelState.inputs) {
                InputSummary(uiState, it, postInput)
            }
        },
        content = {
            if (uiState.focusedViewModelInput != null) {
                InputDetails(uiState, uiState.focusedViewModelInput, postInput)
            }
        }
    )
}

@Suppress("UNUSED_PARAMETER")
@Composable
fun InputSummary(
    uiState: DebuggerUiContract.State,
    inputState: BallastInputState,
    postInput: (DebuggerUiContract.Inputs) -> Unit,
) {
    val timeSinceLastSeen: Duration = (LocalTimer.current - inputState.firstSeen).removeFraction(DurationUnit.SECONDS)
    ContextMenuArea(
        items = {
            buildList<ContextMenuItem> {
                this += ContextMenuItem("Resend Input") {
                    postInput(
                        DebuggerUiContract.Inputs.SendDebuggerAction(
                            BallastDebuggerAction.RequestResendInput(
                                connectionId = inputState.connectionId,
                                viewModelName = inputState.viewModelName,
                                inputUuid = inputState.uuid,
                            )
                        )
                    )
                }
            }
        }
    ) {
        ListItem(
            modifier = Modifier
                .onHoverState { Modifier.highlight() }
                .then(
                    if (uiState.focusedDebuggerEventUuid == inputState.uuid) {
                        Modifier.background(MaterialTheme.colors.onSurface.copy(alpha = 0.1f))
                    } else {
                        Modifier
                    }
                )
                .clickable {
                    postInput(
                        DebuggerUiContract.Inputs.FocusEvent(
                            connectionId = inputState.connectionId,
                            viewModelName = inputState.viewModelName,
                            eventUuid = inputState.uuid,
                        )
                    )
                },
            text = { Text(inputState.type) },
            overlineText = { Text(inputState.status.toString()) },
            secondaryText = { Text("Sent $timeSinceLastSeen ago") },
            trailing = {
                Box {
                    if (inputState.status == BallastInputState.Status.Running) {
                        CircularProgressIndicator()
                    }
                }
            }
        )
    }
}

@Suppress("UNUSED_PARAMETER")
@Composable
fun InputDetails(
    uiState: DebuggerUiContract.State,
    inputState: BallastInputState,
    postInput: (DebuggerUiContract.Inputs) -> Unit,
) {
    val errorStatus = inputState.status as? BallastInputState.Status.Error

    if(errorStatus == null) {
        Box(Modifier.fillMaxSize()) {
            IntellijEditor(inputState.toStringValue)
        }
    } else {
        VSplitPane(
            rememberSplitPaneState(initialPositionPercentage = 0.5f),
            topContent = { IntellijEditor(inputState.toStringValue, Modifier.fillMaxSize()) },
            bottomContent = { IntellijEditor(errorStatus.stacktrace, Modifier.fillMaxSize()) },
        )
    }
}









@Composable
fun ColumnScope.InputsListToolbar(
    connection: BallastConnectionState?,
    viewModel: BallastViewModelState?,
    inputs: List<BallastInputState>,
    postInput: (DebuggerUiContract.Inputs) -> Unit,
) {
}

@Composable
fun ColumnScope.InputsList(
    connection: BallastConnectionState?,
    viewModel: BallastViewModelState?,
    inputs: List<BallastInputState>,
    selectedInput: BallastInputState?,
    postInput: (DebuggerUiContract.Inputs) -> Unit,
) {
}

@Composable
fun ColumnScope.InputDetailsToolbar(
    connection: BallastConnectionState?,
    viewModel: BallastViewModelState?,
    input: BallastInputState?,
    postInput: (DebuggerUiContract.Inputs) -> Unit,
) {
}

@Composable
fun ColumnScope.InputDetails(
    input: BallastInputState?,
    postInput: (DebuggerUiContract.Inputs) -> Unit,
) {
}
