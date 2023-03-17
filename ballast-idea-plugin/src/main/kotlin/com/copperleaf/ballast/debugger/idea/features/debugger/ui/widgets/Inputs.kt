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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.ListItem
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import com.copperleaf.ballast.debugger.idea.features.debugger.router.DebuggerRoute
import com.copperleaf.ballast.debugger.idea.features.debugger.vm.DebuggerUiContract
import com.copperleaf.ballast.debugger.idea.utils.maybeFilter
import com.copperleaf.ballast.debugger.models.BallastConnectionState
import com.copperleaf.ballast.debugger.models.BallastInputState
import com.copperleaf.ballast.debugger.models.BallastViewModelState
import com.copperleaf.ballast.debugger.utils.minus
import com.copperleaf.ballast.debugger.utils.removeFraction
import com.copperleaf.ballast.debugger.versions.v3.BallastDebuggerActionV3
import com.copperleaf.ballast.navigation.routing.Destination
import com.copperleaf.ballast.navigation.routing.build
import com.copperleaf.ballast.navigation.routing.directions
import com.copperleaf.ballast.navigation.routing.pathParameter
import com.copperleaf.ballast.navigation.routing.stringPath
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
                    Column(Modifier.verticalScroll(rememberScrollState())) {
                        Text(
                            text = errorStatus.stacktrace,
                            color = MaterialTheme.colors.error,
                            fontFamily = FontFamily.Monospace,
                        )
                    }
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
                            BallastDebuggerActionV3.RequestResendInput(
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
