@file:Suppress("UNUSED_PARAMETER")

package com.copperleaf.ballast.debugger.idea.features.debugger.ui.widgets

import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import com.copperleaf.ballast.debugger.idea.features.debugger.vm.DebuggerUiContract
import com.copperleaf.ballast.debugger.idea.utils.maybeFilter
import com.copperleaf.ballast.debugger.models.BallastConnectionState
import com.copperleaf.ballast.debugger.models.BallastViewModelState
import com.copperleaf.ballast.debugger.server.vm.DebuggerServerContract
import com.copperleaf.ballast.debugger.versions.v5.BallastDebuggerEventV5

@Composable
internal fun ColumnScope.LogsListToolbar(
    connection: BallastConnectionState?,
    viewModel: BallastViewModelState?,
    fullHistory: List<BallastDebuggerEventV5>,
    postInput: (DebuggerUiContract.Inputs) -> Unit,
) {
    if (connection == null) return
    if (viewModel == null) return

    ToolBarActionIconButton(
        imageVector = Icons.Default.ClearAll,
        contentDescription = "Clear Logs",
        onClick = {
            postInput(
                DebuggerUiContract.Inputs.SendToDebuggerServer(
                    DebuggerServerContract.Inputs.ClearAllLogs(
                        connection.connectionId,
                        viewModel.viewModelName,
                    )
                )
            )
        },
    )
}

@Composable
internal fun ColumnScope.LogsList(
    connection: BallastConnectionState?,
    viewModel: BallastViewModelState?,
    fullHistory: List<BallastDebuggerEventV5>,
    postInput: (DebuggerUiContract.Inputs) -> Unit,
) {
    SelectionContainer {
        Box(Modifier.fillMaxSize()) {
            val scrollState = rememberLazyListState()

            // the list of all Connections
            LazyColumn(Modifier.fillMaxSize(), state = scrollState, reverseLayout = true) {
                items(fullHistory) {
                    LogSummary(it, postInput)
                }
            }

            VerticalScrollbar(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .fillMaxHeight(),
                adapter = rememberScrollbarAdapter(scrollState),
                reverseLayout = true,
            )
        }
    }
}

@Suppress("UNUSED_PARAMETER")
@Composable
internal fun LogSummary(
    logEntry: BallastDebuggerEventV5,
    postInput: (DebuggerUiContract.Inputs) -> Unit,
) {
    val color = when (logEntry) {
        is BallastDebuggerEventV5.ViewModelStatusChanged -> MaterialTheme.colors.onSurface

        is BallastDebuggerEventV5.InputQueued -> MaterialTheme.colors.onSurface
        is BallastDebuggerEventV5.InputAccepted -> MaterialTheme.colors.primary
        is BallastDebuggerEventV5.InputRejected -> MaterialTheme.colors.primary
        is BallastDebuggerEventV5.InputDropped -> MaterialTheme.colors.primary
        is BallastDebuggerEventV5.InputHandledSuccessfully -> MaterialTheme.colors.onSurface
        is BallastDebuggerEventV5.InputCancelled -> MaterialTheme.colors.primary
        is BallastDebuggerEventV5.InputHandlerError -> MaterialTheme.colors.error

        is BallastDebuggerEventV5.EventQueued -> MaterialTheme.colors.onSurface
        is BallastDebuggerEventV5.EventEmitted -> MaterialTheme.colors.primary
        is BallastDebuggerEventV5.EventHandledSuccessfully -> MaterialTheme.colors.onSurface
        is BallastDebuggerEventV5.EventHandlerError -> MaterialTheme.colors.error
        is BallastDebuggerEventV5.EventProcessingStarted -> MaterialTheme.colors.onSurface
        is BallastDebuggerEventV5.EventProcessingStopped -> MaterialTheme.colors.onSurface

        is BallastDebuggerEventV5.StateChanged -> MaterialTheme.colors.primary

        is BallastDebuggerEventV5.SideJobQueued -> MaterialTheme.colors.onSurface
        is BallastDebuggerEventV5.SideJobStarted -> MaterialTheme.colors.primary
        is BallastDebuggerEventV5.SideJobCompleted -> MaterialTheme.colors.onSurface
        is BallastDebuggerEventV5.SideJobCancelled -> MaterialTheme.colors.primary
        is BallastDebuggerEventV5.SideJobError -> MaterialTheme.colors.error

        is BallastDebuggerEventV5.InterceptorAttached -> MaterialTheme.colors.onSurface
        is BallastDebuggerEventV5.InterceptorFailed -> MaterialTheme.colors.error

        is BallastDebuggerEventV5.UnhandledError -> MaterialTheme.colors.error

        is BallastDebuggerEventV5.Heartbeat -> MaterialTheme.colors.onSurface
        is BallastDebuggerEventV5.RefreshViewModelComplete -> MaterialTheme.colors.onSurface
        is BallastDebuggerEventV5.RefreshViewModelStart -> MaterialTheme.colors.onSurface
    }

    val text = remember(logEntry) {
        "[${logEntry.timestamp.format()}] $logEntry"
    }

    Text(
        text = text,
        color = color,
        fontFamily = FontFamily.Monospace,
    )
}

// Data for Logs
// ---------------------------------------------------------------------------------------------------------------------

@Composable
internal fun rememberViewModelLogsList(
    viewModel: BallastViewModelState?,
    searchText: String,
): State<List<BallastDebuggerEventV5>> {
    return viewModelValue {
        viewModel?.fullHistory?.maybeFilter(searchText) {
            listOf(it.toString())
        } ?: emptyList()
    }
}
