@file:Suppress("UNUSED_PARAMETER")

package com.copperleaf.ballast.debugger.idea.ui.debugger.widgets

import androidx.compose.foundation.ContextMenuArea
import androidx.compose.foundation.ContextMenuItem
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
import androidx.compose.material.ListItem
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.copperleaf.ballast.debugger.idea.ui.debugger.DebuggerUiContract
import com.copperleaf.ballast.debugger.models.BallastConnectionState
import com.copperleaf.ballast.debugger.models.BallastStateSnapshot
import com.copperleaf.ballast.debugger.models.BallastViewModelState
import com.copperleaf.ballast.debugger.versions.v3.BallastDebuggerActionV3

@Composable
fun ColumnScope.StatesListToolbar(
    connection: BallastConnectionState?,
    viewModel: BallastViewModelState?,
    states: List<BallastStateSnapshot>,
    postInput: (DebuggerUiContract.Inputs) -> Unit,
) {
}

@Composable
fun ColumnScope.StatesList(
    connection: BallastConnectionState?,
    viewModel: BallastViewModelState?,
    states: List<BallastStateSnapshot>,
    focusedState: BallastStateSnapshot?,
    postInput: (DebuggerUiContract.Inputs) -> Unit,
) {
    Box(Modifier.fillMaxSize()) {
        val scrollState = rememberLazyListState()

        // the list of all Connections
        LazyColumn(Modifier.fillMaxSize(), state = scrollState) {
            items(states) {
                StateSnapshotSummary(it, focusedState, postInput)
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
fun ColumnScope.StateDetailsToolbar(
    connection: BallastConnectionState?,
    viewModel: BallastViewModelState?,
    stateSnapshot: BallastStateSnapshot?,
    postInput: (DebuggerUiContract.Inputs) -> Unit,
) {
}

@Composable
fun ColumnScope.StateDetails(
    stateSnapshot: BallastStateSnapshot?,
    postInput: (DebuggerUiContract.Inputs) -> Unit,
) {
    if(stateSnapshot != null) {
        Box(Modifier.fillMaxSize()) {
            IntellijEditor(stateSnapshot.toStringValue)
        }
    }
}

@Suppress("UNUSED_PARAMETER")
@Composable
fun StateSnapshotSummary(
    stateSnapshot: BallastStateSnapshot,
    focusedState: BallastStateSnapshot?,
    postInput: (DebuggerUiContract.Inputs) -> Unit,
) {
    ContextMenuArea(
        items = {
            buildList<ContextMenuItem> {
                this += ContextMenuItem("Rollback to this State") {
                    postInput(
                        DebuggerUiContract.Inputs.SendDebuggerAction(
                            BallastDebuggerActionV3.RequestRestoreState(
                                connectionId = stateSnapshot.connectionId,
                                viewModelName = stateSnapshot.viewModelName,
                                stateUuid = stateSnapshot.uuid,
                            )
                        )
                    )
                }
            }
        }
    ) {
        ListItem(
            modifier = Modifier
                .onHoverState { Modifier.highlight() }
                .then(
                    if (focusedState?.uuid == stateSnapshot.uuid) {
                        Modifier.background(MaterialTheme.colors.onSurface.copy(alpha = 0.1f))
                    } else {
                        Modifier
                    }
                )
                .clickable {
                    postInput(
                        DebuggerUiContract.Inputs.FocusEvent(
                            connectionId = stateSnapshot.connectionId,
                            viewModelName = stateSnapshot.viewModelName,
                            eventUuid = stateSnapshot.uuid,
                        )
                    )
                }
        ) {
            Text(stateSnapshot.emittedAt.format("hh:mm:ss a"))
        }
    }
}
