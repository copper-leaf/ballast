@file:Suppress("UNUSED_PARAMETER")

package com.copperleaf.ballast.debugger.idea.ui.debugger.widgets

import androidx.compose.foundation.ContextMenuArea
import androidx.compose.foundation.ContextMenuItem
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ListItem
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.copperleaf.ballast.debugger.idea.ui.debugger.DebuggerUiContract
import com.copperleaf.ballast.debugger.models.BallastConnectionState
import com.copperleaf.ballast.debugger.models.BallastDebuggerAction
import com.copperleaf.ballast.debugger.models.BallastStateSnapshot
import com.copperleaf.ballast.debugger.models.BallastViewModelState

@Composable
fun StateSnapshotList(
    uiState: DebuggerUiContract.State,
    viewModelState: BallastViewModelState,
    postInput: (DebuggerUiContract.Inputs) -> Unit,
) {
    SplitPane(
        splitPaneState = uiState.eventsPanePercentage,
        navigation = {
            items(viewModelState.states) {
                StateSnapshotSummary(uiState, it, postInput)
            }
        },
        content = {
            if (uiState.focusedViewModelStateSnapshot != null) {
                StateSnapshotDetails(uiState, uiState.focusedViewModelStateSnapshot, postInput)
            }
        }
    )
}

@Suppress("UNUSED_PARAMETER")
@Composable
fun StateSnapshotSummary(
    uiState: DebuggerUiContract.State,
    stateSnapshot: BallastStateSnapshot,
    postInput: (DebuggerUiContract.Inputs) -> Unit,
) {
    ContextMenuArea(
        items = {
            buildList<ContextMenuItem> {
                this += ContextMenuItem("Rollback to this State") {
                    postInput(
                        DebuggerUiContract.Inputs.SendDebuggerAction(
                            BallastDebuggerAction.RequestRestoreState(
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
                    if (uiState.focusedDebuggerEventUuid == stateSnapshot.uuid) {
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

@Suppress("UNUSED_PARAMETER")
@Composable
fun StateSnapshotDetails(
    uiState: DebuggerUiContract.State,
    stateSnapshot: BallastStateSnapshot,
    postStateSnapshot: (DebuggerUiContract.Inputs) -> Unit,
) {
    Box(Modifier.fillMaxSize()) {
        IntellijEditor(stateSnapshot.toStringValue)
    }
}












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
    selectedState: BallastStateSnapshot?,
    postInput: (DebuggerUiContract.Inputs) -> Unit,
) {
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
}
