@file:Suppress("UNUSED_PARAMETER")

package com.copperleaf.ballast.debugger.idea.ui.debugger.ui.widgets

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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.copperleaf.ballast.debugger.idea.ui.debugger.router.DebuggerRoute
import com.copperleaf.ballast.debugger.idea.ui.debugger.vm.DebuggerUiContract
import com.copperleaf.ballast.debugger.models.BallastConnectionState
import com.copperleaf.ballast.debugger.models.BallastEventState
import com.copperleaf.ballast.debugger.models.BallastViewModelState
import com.copperleaf.ballast.debugger.utils.minus
import com.copperleaf.ballast.debugger.utils.removeFraction
import com.copperleaf.ballast.navigation.routing.build
import com.copperleaf.ballast.navigation.routing.directions
import com.copperleaf.ballast.navigation.routing.pathParameter
import org.jetbrains.compose.splitpane.rememberSplitPaneState
import kotlin.time.Duration
import kotlin.time.DurationUnit

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
    focusedEvent: BallastEventState?,
    postInput: (DebuggerUiContract.Inputs) -> Unit,
) {
    Box(Modifier.fillMaxSize()) {
        val scrollState = rememberLazyListState()

        // the list of all Connections
        LazyColumn(Modifier.fillMaxSize(), state = scrollState) {
            items(events) {
                EventSummary(it, focusedEvent, postInput)
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
    if (event != null) {
        val errorStatus = event.status as? BallastEventState.Status.Error

        if (errorStatus == null) {
            Box(Modifier.fillMaxSize()) {
                IntellijEditor(event.toStringValue)
            }
        } else {
            VSplitPane(
                rememberSplitPaneState(initialPositionPercentage = 0.5f),
                topContent = { IntellijEditor(event.toStringValue, Modifier.fillMaxSize()) },
                bottomContent = { IntellijEditor(errorStatus.stacktrace, Modifier.fillMaxSize()) },
            )
        }
    }
}

@Suppress("UNUSED_PARAMETER")
@Composable
fun EventSummary(
    eventState: BallastEventState,
    focusedEvent: BallastEventState?,
    postInput: (DebuggerUiContract.Inputs) -> Unit,
) {
    val timeSinceLastSeen: Duration = (LocalTimer.current - eventState.firstSeen).removeFraction(DurationUnit.SECONDS)

    ListItem(
        modifier = Modifier
            .onHoverState { Modifier.highlight() }
            .then(
                if (focusedEvent?.uuid == eventState.uuid) {
                    Modifier.background(MaterialTheme.colors.onSurface.copy(alpha = 0.1f))
                } else {
                    Modifier
                }
            )
            .clickable {
                postInput(
                    DebuggerUiContract.Inputs.Navigate(
                        DebuggerRoute.ViewModelEventDetails
                            .directions()
                            .pathParameter("connectionId", eventState.connectionId)
                            .pathParameter("viewModelName", eventState.viewModelName)
                            .pathParameter("eventUuid", eventState.uuid)
                            .build()
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
