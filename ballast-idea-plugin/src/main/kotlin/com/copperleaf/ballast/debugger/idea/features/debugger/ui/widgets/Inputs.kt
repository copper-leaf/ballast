@file:Suppress("UNUSED_PARAMETER")

package com.copperleaf.ballast.debugger.idea.features.debugger.ui.widgets

import androidx.compose.foundation.ContextMenuArea
import androidx.compose.foundation.ContextMenuItem
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.ListItem
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedButton
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.CloudUpload
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
import com.copperleaf.ballast.debugger.models.BallastInputState
import com.copperleaf.ballast.debugger.models.BallastViewModelState
import com.copperleaf.ballast.debugger.utils.minus
import com.copperleaf.ballast.debugger.utils.removeFraction
import com.copperleaf.ballast.debugger.versions.ClientVersion
import com.copperleaf.ballast.debugger.versions.v4.BallastDebuggerActionV4
import com.copperleaf.ballast.navigation.routing.Destination
import com.copperleaf.ballast.navigation.routing.build
import com.copperleaf.ballast.navigation.routing.directions
import com.copperleaf.ballast.navigation.routing.pathParameter
import com.copperleaf.ballast.navigation.routing.stringPath
import io.ktor.http.ContentType
import org.jetbrains.compose.splitpane.rememberSplitPaneState
import kotlin.time.Duration
import kotlin.time.DurationUnit

@Composable
fun ColumnScope.InputsListToolbar(
    connection: BallastConnectionState?,
    viewModel: BallastViewModelState?,
    inputs: List<BallastInputState>,
    postInput: (DebuggerUiContract.Inputs) -> Unit,
) {
    if (connection == null) return
    if (viewModel == null) return

    ToolBarActionIconButton(
        imageVector = Icons.Default.ClearAll,
        contentDescription = "Clear Inputs",
        onClick = { postInput(DebuggerUiContract.Inputs.ClearAllInputs(connection.connectionId, viewModel.viewModelName)) },
    )

    // Replace states from JSON
    val majorVersion = ClientVersion.parse(connection.connectionBallastVersion).major
    var showInputDialog by remember { mutableStateOf(false) }
    var stateContentType by remember { mutableStateOf("application/json") }
    var serializedState by remember { mutableStateOf("") }

    ToolBarActionIconButton(
        imageVector = Icons.Default.CloudUpload,
        enabled = majorVersion >= 4,
        contentDescription = if (majorVersion >= 4) "Send Input" else "Send Input (v4+)",
        onClick = { showInputDialog = true },
    )

    if (showInputDialog) {
        AlertDialog(
            onDismissRequest = { showInputDialog = false },
            confirmButton = {
                Button({
                    postInput(
                        DebuggerUiContract.Inputs.SendDebuggerAction(
                            BallastDebuggerActionV4.RequestSendInput(
                                connection.connectionId,
                                viewModel.viewModelName,
                                serializedInput = serializedState,
                                inputContentType = stateContentType,
                            )
                        )
                    )
                    showInputDialog = false
                }) { Text("Send Input") }
            },
            dismissButton = {
                OutlinedButton({ showInputDialog = false }) { Text("Cancel") }
            },
            title = { Text("Send Input with JSON") },
            text = {
                Column {
                    OutlinedTextField(
                        label = { Text("Content Type") },
                        value = stateContentType,
                        onValueChange = { stateContentType = it },
                    )
                    OutlinedTextField(
                        label = { Text("Serialized Input") },
                        value = serializedState,
                        onValueChange = { serializedState = it },
                    )
                }
            }
        )
    }
}

@Composable
fun ColumnScope.InputsList(
    connection: BallastConnectionState?,
    viewModel: BallastViewModelState?,
    inputs: List<BallastInputState>,
    focusedInput: BallastInputState?,
    postInput: (DebuggerUiContract.Inputs) -> Unit,
) {
    Box(Modifier.fillMaxSize()) {
        val scrollState = rememberLazyListState()

        // the list of all Connections
        LazyColumn(Modifier.fillMaxSize(), state = scrollState) {
            items(inputs) {
                InputSummary(it, focusedInput, postInput)
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
fun ColumnScope.InputDetailsToolbar(
    connection: BallastConnectionState?,
    viewModel: BallastViewModelState?,
    input: BallastInputState?,
    postInput: (DebuggerUiContract.Inputs) -> Unit,
) {
}

@Composable
fun ColumnScope.InputDetails(
    input: BallastInputState?,
    postInput: (DebuggerUiContract.Inputs) -> Unit,
) {
    if (input != null) {
        val errorStatus = input.status as? BallastInputState.Status.Error
        if (errorStatus == null) {
            Box(Modifier.fillMaxSize()) {
                IntellijEditor(input.serializedValue, input.contentType.asContentType())
            }
        } else {
            VSplitPane(
                rememberSplitPaneState(initialPositionPercentage = 0.5f),
                topContent = {
                    IntellijEditor(input.serializedValue, input.contentType.asContentType(), Modifier.fillMaxSize())
                },
                bottomContent = {
                    IntellijEditor(errorStatus.stacktrace, ContentType.Text.Any, Modifier.fillMaxSize(), MaterialTheme.colors.error)
                },
            )
        }
    }
}

@Suppress("UNUSED_PARAMETER")
@Composable
fun InputSummary(
    inputState: BallastInputState,
    focusedInput: BallastInputState?,
    postInput: (DebuggerUiContract.Inputs) -> Unit,
) {
    val timeSinceLastSeen: Duration = (LocalTimer.current - inputState.firstSeen).removeFraction(DurationUnit.SECONDS)
    ContextMenuArea(
        items = {
            buildList<ContextMenuItem> {
                this += ContextMenuItem("Resend Input") {
                    postInput(
                        DebuggerUiContract.Inputs.SendDebuggerAction(
                            BallastDebuggerActionV4.RequestResendInput(
                                connectionId = inputState.connectionId,
                                viewModelName = inputState.viewModelName,
                                inputUuid = inputState.uuid,
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
                    if (focusedInput?.uuid == inputState.uuid) {
                        Modifier.background(MaterialTheme.colors.onSurface.copy(alpha = 0.1f))
                    } else {
                        Modifier
                    }
                )
                .clickable {
                    postInput(
                        DebuggerUiContract.Inputs.Navigate(
                            DebuggerRoute.ViewModelInputDetails
                                .directions()
                                .pathParameter("connectionId", inputState.connectionId)
                                .pathParameter("viewModelName", inputState.viewModelName)
                                .pathParameter("inputUuid", inputState.uuid)
                                .build()
                        )
                    )
                },
            text = { Text(inputState.type) },
            overlineText = { Text(inputState.status.toString()) },
            secondaryText = { Text("Sent $timeSinceLastSeen ago") },
            trailing = {
                Box {
                    if (inputState.status == BallastInputState.Status.Running) {
                        CircularProgressIndicator()
                    }
                }
            }
        )
    }
}

// Data for Inputs
// ---------------------------------------------------------------------------------------------------------------------

@Composable
fun rememberViewModelInputsList(
    viewModel: BallastViewModelState?,
    searchText: String,
): State<List<BallastInputState>> {
    return viewModelValue {
        viewModel?.inputs?.maybeFilter(searchText) {
            listOf(it.type, it.serializedValue)
        } ?: emptyList()
    }
}

@Composable
fun Destination.ParametersProvider.rememberSelectedViewModelInput(
    viewModel: BallastViewModelState?,
): State<BallastInputState?> {
    return viewModelValue {
        val inputUuid: String by stringPath()
        viewModel?.inputs?.find { it.uuid == inputUuid }
    }
}
