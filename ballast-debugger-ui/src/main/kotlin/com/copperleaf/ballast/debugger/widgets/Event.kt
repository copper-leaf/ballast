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
import com.copperleaf.ballast.debugger.models.BallastEventState
import com.copperleaf.ballast.debugger.models.BallastViewModelState
import com.copperleaf.ballast.debugger.utils.minus
import com.copperleaf.ballast.debugger.utils.removeFraction
import com.copperleaf.ballast.debugger.windows.debugger.DebuggerContract
import org.jetbrains.compose.splitpane.ExperimentalSplitPaneApi
import kotlin.time.Duration
import kotlin.time.DurationUnit

@OptIn(ExperimentalSplitPaneApi::class, ExperimentalFoundationApi::class, ExperimentalMaterialApi::class)
@Composable
fun EventList(
    uiState: DebuggerContract.State,
    viewModelState: BallastViewModelState,
    postInput: (DebuggerContract.Inputs) -> Unit,
) {
    SplitPane(
        initialPositionPercentage = 0.5f,
        navigation = {
            items(viewModelState.events) {
                EventSummary(uiState, it, postInput)
            }
        },
        content = {
            if (uiState.focusedViewModelEvent != null) {
                EventDetails(uiState, uiState.focusedViewModelEvent, postInput)
            }
        }
    )
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun EventSummary(
    uiState: DebuggerContract.State,
    eventState: BallastEventState,
    postInput: (DebuggerContract.Inputs) -> Unit,
) {
    val timeSinceLastSeen: Duration = (LocalTimer.current - eventState.firstSeen).removeFraction(DurationUnit.SECONDS)

    ListItem(
        modifier = Modifier
            .clickable {
                postInput(
                    DebuggerContract.Inputs.FocusEvent(
                        connectionId = eventState.connectionId,
                        viewModelName = eventState.viewModelName,
                        eventUuid = eventState.uuid,
                    )
                )
            },
        text = { Text(eventState.type) },
        overlineText = { Text(eventState.status.toString()) },
        secondaryText = { Text("Sent $timeSinceLastSeen ago") },
        trailing = {
            Box {
                if (eventState.status == BallastEventState.Status.Running) {
                    CircularProgressIndicator()
                }
            }
        }
    )
}

@Composable
fun EventDetails(
    uiState: DebuggerContract.State,
    eventState: BallastEventState,
    postInput: (DebuggerContract.Inputs) -> Unit,
) {
    SelectionContainer {
        Column {
            Text(eventState.toStringValue)

            (eventState.status as? BallastEventState.Status.Error)?.let {
                Text(it.stacktrace, fontFamily = FontFamily.Monospace)
            }
        }
    }
}
