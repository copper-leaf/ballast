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
import com.copperleaf.ballast.debugger.models.BallastEventState
import com.copperleaf.ballast.debugger.models.BallastViewModelState
import com.copperleaf.ballast.debugger.utils.minus
import com.copperleaf.ballast.debugger.utils.removeFraction
import org.jetbrains.compose.splitpane.rememberSplitPaneState
import kotlin.time.Duration
import kotlin.time.DurationUnit

@Composable
fun EventList(
    uiState: DebuggerUiContract.State,
    viewModelState: BallastViewModelState,
    postInput: (DebuggerUiContract.Inputs) -> Unit,
) {
    SplitPane(
        splitPaneState = uiState.eventsPanePercentage,
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

@Suppress("UNUSED_PARAMETER")
@Composable
fun EventSummary(
    uiState: DebuggerUiContract.State,
    eventState: BallastEventState,
    postInput: (DebuggerUiContract.Inputs) -> Unit,
) {
    val timeSinceLastSeen: Duration = (LocalTimer.current - eventState.firstSeen).removeFraction(DurationUnit.SECONDS)

    ListItem(
        modifier = Modifier
            .onHoverState { Modifier.highlight() }
            .then(
                if (uiState.focusedDebuggerEventUuid == eventState.uuid) {
                    Modifier.background(MaterialTheme.colors.onSurface.copy(alpha = 0.1f))
                } else {
                    Modifier
                }
            )
            .clickable {
                postInput(
                    DebuggerUiContract.Inputs.FocusEvent(
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

@Suppress("UNUSED_PARAMETER")
@Composable
fun EventDetails(
    uiState: DebuggerUiContract.State,
    eventState: BallastEventState,
    postInput: (DebuggerUiContract.Inputs) -> Unit,
) {
    val errorStatus = eventState.status as? BallastEventState.Status.Error

    if(errorStatus == null) {
        Box(Modifier.fillMaxSize()) {
            IntellijEditor(eventState.toStringValue)
        }
    } else {
        VSplitPane(
            rememberSplitPaneState(initialPositionPercentage = 0.5f),
            topContent = { IntellijEditor(eventState.toStringValue, Modifier.fillMaxSize()) },
            bottomContent = { IntellijEditor(errorStatus.stacktrace, Modifier.fillMaxSize()) },
        )
    }
}







@Composable
fun ColumnScope.EventsListToolbar(
    connection: BallastConnectionState?,
    viewModel: BallastViewModelState?,
    events: List<BallastEventState>,
    postInput: (DebuggerUiContract.Inputs) -> Unit,
) {
}

@Composable
fun ColumnScope.EventsList(
    connection: BallastConnectionState?,
    viewModel: BallastViewModelState?,
    events: List<BallastEventState>,
    selectedEvent: BallastEventState?,
    postInput: (DebuggerUiContract.Inputs) -> Unit,
) {
}

@Composable
fun ColumnScope.EventDetailsToolbar(
    connection: BallastConnectionState?,
    viewModel: BallastViewModelState?,
    event: BallastEventState?,
    postInput: (DebuggerUiContract.Inputs) -> Unit,
) {
}

@Composable
fun ColumnScope.EventDetails(
    event: BallastEventState?,
    postInput: (DebuggerUiContract.Inputs) -> Unit,
) {
}