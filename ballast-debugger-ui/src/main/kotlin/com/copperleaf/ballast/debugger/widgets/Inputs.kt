package com.copperleaf.ballast.debugger.widgets

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ListItem
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import com.copperleaf.ballast.debugger.models.BallastInputState
import com.copperleaf.ballast.debugger.models.BallastViewModelState
import com.copperleaf.ballast.debugger.utils.minus
import com.copperleaf.ballast.debugger.utils.removeFraction
import com.copperleaf.ballast.debugger.windows.debugger.DebuggerContract
import org.jetbrains.compose.splitpane.ExperimentalSplitPaneApi
import kotlin.time.Duration
import kotlin.time.DurationUnit

@OptIn(ExperimentalSplitPaneApi::class, ExperimentalFoundationApi::class, ExperimentalMaterialApi::class)
@Composable
fun InputList(
    uiState: DebuggerContract.State,
    viewModelState: BallastViewModelState,
    postInput: (DebuggerContract.Inputs) -> Unit,
) {
    SplitPane(
        initialPositionPercentage = 0.45f,
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

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun InputSummary(
    uiState: DebuggerContract.State,
    inputState: BallastInputState,
    postInput: (DebuggerContract.Inputs) -> Unit,
) {
    val timeSinceLastSeen: Duration = (LocalTimer.current - inputState.firstSeen).removeFraction(DurationUnit.SECONDS)

    ListItem(
        modifier = Modifier
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
