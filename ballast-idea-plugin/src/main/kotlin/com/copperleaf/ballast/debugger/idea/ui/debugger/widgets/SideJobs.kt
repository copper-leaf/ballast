package com.copperleaf.ballast.debugger.idea.ui.debugger.widgets

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
import com.copperleaf.ballast.debugger.models.BallastSideJobState
import com.copperleaf.ballast.debugger.models.BallastViewModelState
import com.copperleaf.ballast.debugger.utils.minus
import com.copperleaf.ballast.debugger.utils.removeFraction
import org.jetbrains.compose.splitpane.rememberSplitPaneState
import kotlin.time.Duration
import kotlin.time.DurationUnit

@Composable
fun SideJobList(
    uiState: DebuggerUiContract.State,
    viewModelState: BallastViewModelState,
    postInput: (DebuggerUiContract.Inputs) -> Unit,
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
    uiState: DebuggerUiContract.State,
    sideJobState: BallastSideJobState,
    postInput: (DebuggerUiContract.Inputs) -> Unit,
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
                    DebuggerUiContract.Inputs.FocusEvent(
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
    uiState: DebuggerUiContract.State,
    sideJobState: BallastSideJobState,
    postInput: (DebuggerUiContract.Inputs) -> Unit,
) {
    val errorStatus = sideJobState.status as? BallastSideJobState.Status.Error

    if(errorStatus == null) {
        Box(Modifier.fillMaxSize()) {
            IntellijEditor(sideJobState.key)
        }
    } else {
        VSplitPane(
            rememberSplitPaneState(initialPositionPercentage = 0.5f),
            topContent = { IntellijEditor(sideJobState.key, Modifier.fillMaxSize()) },
            bottomContent = { IntellijEditor(errorStatus.stacktrace, Modifier.fillMaxSize()) },
        )
    }
}












@Composable
fun ColumnScope.SideJobsListToolbar(
    connection: BallastConnectionState?,
    viewModel: BallastViewModelState?,
    sideJobs: List<BallastSideJobState>,
    postInput: (DebuggerUiContract.Inputs) -> Unit,
) {
}

@Composable
fun ColumnScope.SideJobsList(
    connection: BallastConnectionState?,
    viewModel: BallastViewModelState?,
    sideJobs: List<BallastSideJobState>,
    selectedSideJob: BallastSideJobState?,
    postInput: (DebuggerUiContract.Inputs) -> Unit,
) {
}

@Composable
fun ColumnScope.SideJobDetailsToolbar(
    connection: BallastConnectionState?,
    viewModel: BallastViewModelState?,
    sideJob: BallastSideJobState?,
    postInput: (DebuggerUiContract.Inputs) -> Unit,
) {
}

@Composable
fun ColumnScope.SideJobDetails(
    sideJob: BallastSideJobState?,
    postInput: (DebuggerUiContract.Inputs) -> Unit,
) {
}
