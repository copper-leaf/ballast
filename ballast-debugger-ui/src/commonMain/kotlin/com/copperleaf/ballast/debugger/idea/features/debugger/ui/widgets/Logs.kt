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
import com.copperleaf.ballast.debugger.versions.v4.BallastDebuggerActionV4
import com.copperleaf.ballast.debugger.versions.v4.BallastDebuggerEventV4

@Composable
internal fun ColumnScope.LogsListToolbar(
    connection: BallastConnectionState?,
    viewModel: BallastViewModelState?,
    fullHistory: List<BallastDebuggerEventV4>,
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
    fullHistory: List<BallastDebuggerEventV4>,
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
    logEntry: BallastDebuggerEventV4,
    postInput: (DebuggerUiContract.Inputs) -> Unit,
) {
    val color = when (logEntry) {
        is BallastDebuggerEventV4.ViewModelStatusChanged -> MaterialTheme.colors.onSurface

        is BallastDebuggerEventV4.InputQueued -> MaterialTheme.colors.onSurface
        is BallastDebuggerEventV4.InputAccepted -> MaterialTheme.colors.primary
        is BallastDebuggerEventV4.InputRejected -> MaterialTheme.colors.primary
        is BallastDebuggerEventV4.InputDropped -> MaterialTheme.colors.primary
        is BallastDebuggerEventV4.InputHandledSuccessfully -> MaterialTheme.colors.onSurface
        is BallastDebuggerEventV4.InputCancelled -> MaterialTheme.colors.primary
        is BallastDebuggerEventV4.InputHandlerError -> MaterialTheme.colors.error

        is BallastDebuggerEventV4.EventQueued -> MaterialTheme.colors.onSurface
        is BallastDebuggerEventV4.EventEmitted -> MaterialTheme.colors.primary
        is BallastDebuggerEventV4.EventHandledSuccessfully -> MaterialTheme.colors.onSurface
        is BallastDebuggerEventV4.EventHandlerError -> MaterialTheme.colors.error
        is BallastDebuggerEventV4.EventProcessingStarted -> MaterialTheme.colors.onSurface
        is BallastDebuggerEventV4.EventProcessingStopped -> MaterialTheme.colors.onSurface

        is BallastDebuggerEventV4.StateChanged -> MaterialTheme.colors.primary

        is BallastDebuggerEventV4.SideJobQueued -> MaterialTheme.colors.onSurface
        is BallastDebuggerEventV4.SideJobStarted -> MaterialTheme.colors.primary
        is BallastDebuggerEventV4.SideJobCompleted -> MaterialTheme.colors.onSurface
        is BallastDebuggerEventV4.SideJobCancelled -> MaterialTheme.colors.primary
        is BallastDebuggerEventV4.SideJobError -> MaterialTheme.colors.error

        is BallastDebuggerEventV4.InterceptorAttached -> MaterialTheme.colors.onSurface
        is BallastDebuggerEventV4.InterceptorFailed -> MaterialTheme.colors.error

        is BallastDebuggerEventV4.UnhandledError -> MaterialTheme.colors.error

        is BallastDebuggerEventV4.Heartbeat -> MaterialTheme.colors.onSurface
        is BallastDebuggerEventV4.RefreshViewModelComplete -> MaterialTheme.colors.onSurface
        is BallastDebuggerEventV4.RefreshViewModelStart -> MaterialTheme.colors.onSurface
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
): State<List<BallastDebuggerEventV4>> {
    return viewModelValue {
        viewModel?.fullHistory?.maybeFilter(searchText) {
            listOf(it.toString())
        } ?: emptyList()
    }
}
