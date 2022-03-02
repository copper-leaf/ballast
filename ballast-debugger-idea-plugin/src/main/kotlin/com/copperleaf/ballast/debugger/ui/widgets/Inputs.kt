package com.copperleaf.ballast.debugger.ui.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.ListItem
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import com.copperleaf.ballast.debugger.models.BallastInputState
import com.copperleaf.ballast.debugger.models.BallastViewModelState
import com.copperleaf.ballast.debugger.ui.debugger.DebuggerContract
import com.copperleaf.ballast.debugger.utils.minus
import com.copperleaf.ballast.debugger.utils.removeFraction
import kotlin.time.Duration
import kotlin.time.DurationUnit

@Composable
fun InputList(
    uiState: DebuggerContract.State,
    viewModelState: BallastViewModelState,
    postInput: (DebuggerContract.Inputs) -> Unit,
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
    uiState: DebuggerContract.State,
    inputState: BallastInputState,
    postInput: (DebuggerContract.Inputs) -> Unit,
) {
    val timeSinceLastSeen: Duration = (LocalTimer.current - inputState.firstSeen).removeFraction(DurationUnit.SECONDS)

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
                    DebuggerContract.Inputs.FocusEvent(
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

@Suppress("UNUSED_PARAMETER")
@Composable
fun InputDetails(
    uiState: DebuggerContract.State,
    inputState: BallastInputState,
    postInput: (DebuggerContract.Inputs) -> Unit,
) {
    SelectionContainer {
        Column {
            Text(inputState.toStringValue)

            (inputState.status as? BallastInputState.Status.Error)?.let {
                Text(it.stacktrace, fontFamily = FontFamily.Monospace)
            }
        }
    }
}