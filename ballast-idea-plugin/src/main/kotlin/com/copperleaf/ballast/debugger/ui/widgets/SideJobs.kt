package com.copperleaf.ballast.debugger.ui.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Divider
import androidx.compose.material.ListItem
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import com.copperleaf.ballast.debugger.models.BallastSideJobState
import com.copperleaf.ballast.debugger.models.BallastViewModelState
import com.copperleaf.ballast.debugger.ui.debugger.DebuggerContract
import com.copperleaf.ballast.debugger.utils.minus
import com.copperleaf.ballast.debugger.utils.removeFraction
import kotlin.time.Duration
import kotlin.time.DurationUnit

@Composable
fun SideJobList(
    uiState: DebuggerContract.State,
    viewModelState: BallastViewModelState,
    postInput: (DebuggerContract.Inputs) -> Unit,
) {
    SplitPane(
        splitPaneState = uiState.eventsPanePercentage,
        navigation = {
            items(viewModelState.sideJobs) {
                SideJobSummary(uiState, it, postInput)
            }
        },
        content = {
            if (uiState.focusedViewModelSideJob != null) {
                SideJobDetails(uiState, uiState.focusedViewModelSideJob, postInput)
            }
        }
    )
}

@Suppress("UNUSED_PARAMETER")
@Composable
fun SideJobSummary(
    uiState: DebuggerContract.State,
    sideJobState: BallastSideJobState,
    postInput: (DebuggerContract.Inputs) -> Unit,
) {
    val timeSinceLastSeen: Duration = (LocalTimer.current - sideJobState.firstSeen)
        .removeFraction(DurationUnit.SECONDS)

    ListItem(
        modifier = Modifier
            .onHoverState { Modifier.highlight() }
            .then(
                if (uiState.focusedDebuggerEventUuid == sideJobState.uuid) {
                    Modifier.background(MaterialTheme.colors.onSurface.copy(alpha = 0.1f))
                } else {
                    Modifier
                }
            )
            .clickable {
                postInput(
                    DebuggerContract.Inputs.FocusEvent(
                        connectionId = sideJobState.connectionId,
                        viewModelName = sideJobState.viewModelName,
                        eventUuid = sideJobState.uuid,
                    )
                )
            },
        text = { Text(sideJobState.key) },
        overlineText = { Text(sideJobState.status.toString()) },
        secondaryText = { Text("${sideJobState.restartState} - Sent $timeSinceLastSeen ago") },
        trailing = {
            Box {
                if (sideJobState.status == BallastSideJobState.Status.Running) {
                    CircularProgressIndicator()
                }
            }
        },
    )
}

@Suppress("UNUSED_PARAMETER")
@Composable
fun SideJobDetails(
    uiState: DebuggerContract.State,
    sideJobState: BallastSideJobState,
    postInput: (DebuggerContract.Inputs) -> Unit,
) {
    SelectionContainer {
        Column {
            Text(sideJobState.key)

            (sideJobState.status as? BallastSideJobState.Status.Error)?.let {
                Divider()
                Text(it.stacktrace, fontFamily = FontFamily.Monospace)
            }
        }
    }
}
