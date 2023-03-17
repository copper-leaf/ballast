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
import com.copperleaf.ballast.debugger.versions.v3.BallastDebuggerEventV3

@Composable
fun ColumnScope.LogsListToolbar(
    connection: BallastConnectionState?,
    viewModel: BallastViewModelState?,
    fullHistory: List<BallastDebuggerEventV3>,
    postInput: (DebuggerUiContract.Inputs) -> Unit,
) {
}

@Composable
fun ColumnScope.LogsList(
    connection: BallastConnectionState?,
    viewModel: BallastViewModelState?,
    fullHistory: List<BallastDebuggerEventV3>,
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
fun LogSummary(
    logEntry: BallastDebuggerEventV3,
    postInput: (DebuggerUiContract.Inputs) -> Unit,
) {
    val color = when (logEntry) {
        is BallastDebuggerEventV3.ViewModelStatusChanged -> MaterialTheme.colors.onSurface

        is BallastDebuggerEventV3.InputQueued -> MaterialTheme.colors.onSurface
        is BallastDebuggerEventV3.InputAccepted -> MaterialTheme.colors.primary
        is BallastDebuggerEventV3.InputRejected -> MaterialTheme.colors.primary
        is BallastDebuggerEventV3.InputDropped -> MaterialTheme.colors.primary
        is BallastDebuggerEventV3.InputHandledSuccessfully -> MaterialTheme.colors.onSurface
        is BallastDebuggerEventV3.InputCancelled -> MaterialTheme.colors.primary
        is BallastDebuggerEventV3.InputHandlerError -> MaterialTheme.colors.error

        is BallastDebuggerEventV3.EventQueued -> MaterialTheme.colors.onSurface
        is BallastDebuggerEventV3.EventEmitted -> MaterialTheme.colors.primary
        is BallastDebuggerEventV3.EventHandledSuccessfully -> MaterialTheme.colors.onSurface
        is BallastDebuggerEventV3.EventHandlerError -> MaterialTheme.colors.error
        is BallastDebuggerEventV3.EventProcessingStarted -> MaterialTheme.colors.onSurface
        is BallastDebuggerEventV3.EventProcessingStopped -> MaterialTheme.colors.onSurface

        is BallastDebuggerEventV3.StateChanged -> MaterialTheme.colors.primary

        is BallastDebuggerEventV3.SideJobQueued -> MaterialTheme.colors.onSurface
        is BallastDebuggerEventV3.SideJobStarted -> MaterialTheme.colors.primary
        is BallastDebuggerEventV3.SideJobCompleted -> MaterialTheme.colors.onSurface
        is BallastDebuggerEventV3.SideJobCancelled -> MaterialTheme.colors.primary
        is BallastDebuggerEventV3.SideJobError -> MaterialTheme.colors.error

        is BallastDebuggerEventV3.InterceptorAttached -> MaterialTheme.colors.onSurface
        is BallastDebuggerEventV3.InterceptorFailed -> MaterialTheme.colors.error

        is BallastDebuggerEventV3.UnhandledError -> MaterialTheme.colors.error

        is BallastDebuggerEventV3.Heartbeat -> MaterialTheme.colors.onSurface
        is BallastDebuggerEventV3.RefreshViewModelComplete -> MaterialTheme.colors.onSurface
        is BallastDebuggerEventV3.RefreshViewModelStart -> MaterialTheme.colors.onSurface
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
fun rememberViewModelLogsList(
    viewModel: BallastViewModelState?,
    searchText: String,
): State<List<BallastDebuggerEventV3>> {
    return viewModelValue {
        viewModel?.fullHistory?.maybeFilter(searchText) {
            listOf(it.toString())
        } ?: emptyList()
    }
}
