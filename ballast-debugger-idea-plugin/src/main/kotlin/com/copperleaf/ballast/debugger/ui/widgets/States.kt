package com.copperleaf.ballast.debugger.ui.widgets

import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.ListItem
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.copperleaf.ballast.debugger.models.BallastStateSnapshot
import com.copperleaf.ballast.debugger.models.BallastViewModelState
import com.copperleaf.ballast.debugger.ui.debugger.DebuggerContract

@Composable
fun StateSnapshotList(
    uiState: DebuggerContract.State,
    viewModelState: BallastViewModelState,
    postInput: (DebuggerContract.Inputs) -> Unit,
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

@Composable
fun StateSnapshotSummary(
    uiState: DebuggerContract.State,
    stateSnapshot: BallastStateSnapshot,
    postInput: (DebuggerContract.Inputs) -> Unit,
) {
    ListItem(
        modifier = Modifier
            .clickable {
                postInput(
                    DebuggerContract.Inputs.FocusEvent(
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

@Composable
fun StateSnapshotDetails(
    uiState: DebuggerContract.State,
    StateSnapshotState: BallastStateSnapshot,
    postStateSnapshot: (DebuggerContract.Inputs) -> Unit,
) {
    SelectionContainer {
        Text(StateSnapshotState.toStringValue)
    }
}
