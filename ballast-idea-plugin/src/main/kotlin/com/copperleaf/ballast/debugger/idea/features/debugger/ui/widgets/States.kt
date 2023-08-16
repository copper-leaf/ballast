@file:Suppress("UNUSED_PARAMETER")

package com.copperleaf.ballast.debugger.idea.features.debugger.ui.widgets

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
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.ListItem
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedButton
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.copperleaf.ballast.debugger.idea.features.debugger.router.DebuggerRoute
import com.copperleaf.ballast.debugger.idea.features.debugger.vm.DebuggerUiContract
import com.copperleaf.ballast.debugger.idea.utils.maybeFilter
import com.copperleaf.ballast.debugger.models.BallastConnectionState
import com.copperleaf.ballast.debugger.models.BallastStateSnapshot
import com.copperleaf.ballast.debugger.models.BallastViewModelState
import com.copperleaf.ballast.debugger.versions.ClientVersion
import com.copperleaf.ballast.debugger.versions.v4.BallastDebuggerActionV4
import com.copperleaf.ballast.navigation.routing.Destination
import com.copperleaf.ballast.navigation.routing.build
import com.copperleaf.ballast.navigation.routing.directions
import com.copperleaf.ballast.navigation.routing.pathParameter
import com.copperleaf.ballast.navigation.routing.stringPath

@Composable
fun ColumnScope.StatesListToolbar(
    connection: BallastConnectionState?,
    viewModel: BallastViewModelState?,
    states: List<BallastStateSnapshot>,
    postInput: (DebuggerUiContract.Inputs) -> Unit,
) {
    val majorVersion = ClientVersion.parse(connection?.connectionBallastVersion).major

    // Replace states from JSON
    var showInputDialog by remember { mutableStateOf(false) }
    var serializedState by remember { mutableStateOf("") }

    ToolBarActionIconButton(
        imageVector = Icons.Default.CloudSync,
        enabled = majorVersion >= 4,
        contentDescription = if(majorVersion >= 4) "Replace State" else "Replace State (v4+)",
        onClick = { showInputDialog = true },
    )

    if (showInputDialog) {
        AlertDialog(
            onDismissRequest = { showInputDialog = false },
            confirmButton = {
                Button({
                    postInput(
                        DebuggerUiContract.Inputs.SendDebuggerAction(
                            BallastDebuggerActionV4.RequestReplaceState(
                                connection!!.connectionId,
                                viewModel!!.viewModelName,
                                serializedState = serializedState,
                                stateContentType = "application/json"
                            )
                        )
                    )
                    showInputDialog = false
                }) { Text("Send State") }
            },
            dismissButton = {
                OutlinedButton({ showInputDialog = false }) { Text("Cancel") }
            },
            title = { Text("Replace state with JSON") },
            text = {
                OutlinedTextField(
                    value = serializedState,
                    onValueChange = { serializedState = it },
                )
            }
        )
    }
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
    if (stateSnapshot != null) {
        Box(Modifier.fillMaxSize()) {
            IntellijEditor(stateSnapshot.serializedValue, stateSnapshot.contentType.asContentType())
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
                            BallastDebuggerActionV4.RequestRestoreState(
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
                        DebuggerUiContract.Inputs.Navigate(
                            DebuggerRoute.ViewModelStateDetails
                                .directions()
                                .pathParameter("connectionId", stateSnapshot.connectionId)
                                .pathParameter("viewModelName", stateSnapshot.viewModelName)
                                .pathParameter("stateUuid", stateSnapshot.uuid)
                                .build()
                        )
                    )
                }
        ) {
            Text(stateSnapshot.emittedAt.format())
        }
    }
}

// Data for States
// ---------------------------------------------------------------------------------------------------------------------


@Composable
fun rememberViewModelStatesList(
    viewModel: BallastViewModelState?,
    searchText: String,
): State<List<BallastStateSnapshot>> {
    return viewModelValue {
        viewModel?.states?.maybeFilter(searchText) {
            listOf(it.serializedValue)
        } ?: emptyList()
    }
}

@Composable
fun Destination.ParametersProvider.rememberSelectedViewModelStateSnapshot(
    viewModel: BallastViewModelState?,
): State<BallastStateSnapshot?> {
    return viewModelValue {
        val stateUuid: String by stringPath()
        viewModel?.states?.find { state -> state.uuid == stateUuid }
    }
}

@Composable
fun rememberLatestViewModelStateSnapshot(
    viewModel: BallastViewModelState?,
): State<BallastStateSnapshot?> {
    return viewModelValue {
        viewModel?.states?.firstOrNull()
    }
}
