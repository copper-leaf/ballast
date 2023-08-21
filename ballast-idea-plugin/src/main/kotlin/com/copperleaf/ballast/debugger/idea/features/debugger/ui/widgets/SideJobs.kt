@file:Suppress("UNUSED_PARAMETER")

package com.copperleaf.ballast.debugger.idea.features.debugger.ui.widgets

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.copperleaf.ballast.debugger.idea.features.debugger.router.DebuggerRoute
import com.copperleaf.ballast.debugger.idea.features.debugger.vm.DebuggerUiContract
import com.copperleaf.ballast.debugger.idea.utils.maybeFilter
import com.copperleaf.ballast.debugger.models.BallastConnectionState
import com.copperleaf.ballast.debugger.models.BallastSideJobState
import com.copperleaf.ballast.debugger.models.BallastViewModelState
import com.copperleaf.ballast.debugger.utils.minus
import com.copperleaf.ballast.debugger.utils.removeFraction
import com.copperleaf.ballast.navigation.routing.Destination
import com.copperleaf.ballast.navigation.routing.build
import com.copperleaf.ballast.navigation.routing.directions
import com.copperleaf.ballast.navigation.routing.pathParameter
import com.copperleaf.ballast.navigation.routing.stringPath
import io.ktor.http.ContentType
import org.jetbrains.compose.splitpane.rememberSplitPaneState
import kotlin.time.Duration
import kotlin.time.DurationUnit

@Composable
fun ColumnScope.SideJobsListToolbar(
    connection: BallastConnectionState?,
    viewModel: BallastViewModelState?,
    sideJobs: List<BallastSideJobState>,
    postInput: (DebuggerUiContract.Inputs) -> Unit,
) {
    if (connection == null) return
    if (viewModel == null) return

    ToolBarActionIconButton(
        imageVector = Icons.Default.ClearAll,
        contentDescription = "Clear Side Jobs",
        onClick = {
            postInput(
                DebuggerUiContract.Inputs.ClearAllSideJobs(
                    connection.connectionId,
                    viewModel.viewModelName
                )
            )
        },
    )
}

@Composable
fun ColumnScope.SideJobsList(
    connection: BallastConnectionState?,
    viewModel: BallastViewModelState?,
    sideJobs: List<BallastSideJobState>,
    focusedSideJob: BallastSideJobState?,
    postInput: (DebuggerUiContract.Inputs) -> Unit,
) {
    Box(Modifier.fillMaxSize()) {
        val scrollState = rememberLazyListState()

        // the list of all Connections
        LazyColumn(Modifier.fillMaxSize(), state = scrollState) {
            items(sideJobs) {
                SideJobSummary(it, focusedSideJob, postInput)
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
    if (sideJob != null) {
        val errorStatus = sideJob.status as? BallastSideJobState.Status.Error

        if (errorStatus == null) {
            Box(Modifier.fillMaxSize()) {
                IntellijEditor(
                    sideJob.key,
                    ContentType.Text.Any,
                    onContentCopied = { postInput(DebuggerUiContract.Inputs.CopyToClipboard(it)) },
                )
            }
        } else {
            VSplitPane(
                rememberSplitPaneState(initialPositionPercentage = 0.5f),
                topContent = {
                    IntellijEditor(
                        sideJob.key,
                        ContentType.Text.Any,
                        Modifier.fillMaxSize(),
                        onContentCopied = { postInput(DebuggerUiContract.Inputs.CopyToClipboard(it)) },
                    )
                },
                bottomContent = {
                    IntellijEditor(
                        errorStatus.stacktrace,
                        ContentType.Text.Any,
                        Modifier.fillMaxSize(),
                        MaterialTheme.colors.error,
                        onContentCopied = { postInput(DebuggerUiContract.Inputs.CopyToClipboard(it)) },
                    )
                },
            )
        }
    }
}

@Suppress("UNUSED_PARAMETER")
@Composable
fun SideJobSummary(
    sideJobState: BallastSideJobState,
    focusedSideJob: BallastSideJobState?,
    postInput: (DebuggerUiContract.Inputs) -> Unit,
) {
    val timeSinceLastSeen: Duration = (LocalTimer.current - sideJobState.firstSeen)
        .removeFraction(DurationUnit.SECONDS)

    ListItem(
        modifier = Modifier
            .onHoverState { Modifier.highlight() }
            .then(
                if (focusedSideJob?.uuid == sideJobState.uuid) {
                    Modifier.background(MaterialTheme.colors.onSurface.copy(alpha = 0.1f))
                } else {
                    Modifier
                }
            )
            .clickable {
                postInput(
                    DebuggerUiContract.Inputs.Navigate(
                        DebuggerRoute.ViewModelSideJobDetails
                            .directions()
                            .pathParameter("connectionId", sideJobState.connectionId)
                            .pathParameter("viewModelName", sideJobState.viewModelName)
                            .pathParameter("sideJobUuid", sideJobState.uuid)
                            .build()
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

// Data for SideJobs
// ---------------------------------------------------------------------------------------------------------------------

@Composable
fun rememberViewModelSideJobsList(
    viewModel: BallastViewModelState?,
    searchText: String,
): State<List<BallastSideJobState>> {
    return viewModelValue {
        viewModel?.sideJobs?.maybeFilter(searchText) {
            listOf(it.key)
        } ?: emptyList()
    }
}

@Composable
fun Destination.ParametersProvider.rememberSelectedViewModelSideJob(
    viewModel: BallastViewModelState?,
): State<BallastSideJobState?> {
    return viewModelValue {
        val sideJobUuid: String by stringPath()
        viewModel?.sideJobs?.find { it.uuid == sideJobUuid }
    }
}
