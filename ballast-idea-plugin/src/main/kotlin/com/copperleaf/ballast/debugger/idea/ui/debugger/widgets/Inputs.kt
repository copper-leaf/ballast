@file:Suppress("UNUSED_PARAMETER")

package com.copperleaf.ballast.debugger.idea.ui.debugger.widgets

import androidx.compose.foundation.ContextMenuArea
import androidx.compose.foundation.ContextMenuItem
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.ListItem
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
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
    focusedInput: BallastInputState?,
    postInput: (DebuggerUiContract.Inputs) -> Unit,
) {
    Box(Modifier.fillMaxSize()) {
        val scrollState = rememberLazyListState()

        // the list of all Connections
        LazyColumn(Modifier.fillMaxSize(), state = scrollState) {
            items(inputs) {
                InputSummary(it, focusedInput, postInput)
            }
        }

        VerticalScrollbar(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .fillMaxHeight(),
            adapter = rememberScrollbarAdapter(scrollState),
        )
    }
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
    if (input != null) {
        val errorStatus = input.status as? BallastInputState.Status.Error
        if (errorStatus == null) {
            Box(Modifier.fillMaxSize()) {
                IntellijEditor(input.toStringValue)
            }
        } else {
            VSplitPane(
                rememberSplitPaneState(initialPositionPercentage = 0.5f),
                topContent = { IntellijEditor(input.toStringValue, Modifier.fillMaxSize()) },
                bottomContent = { IntellijEditor(errorStatus.stacktrace, Modifier.fillMaxSize()) },
            )
        }
    }
}

@Suppress("UNUSED_PARAMETER")
@Composable
fun InputSummary(
    inputState: BallastInputState,
    focusedInput: BallastInputState?,
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
                    if (focusedInput?.uuid == inputState.uuid) {
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
