@file:Suppress("UNUSED_PARAMETER")
package com.copperleaf.ballast.debugger.idea.features.debugger.ui.widgets

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.Icon
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.copperleaf.ballast.debugger.idea.features.debugger.vm.DebuggerUiContract
import com.copperleaf.ballast.debugger.idea.features.debugger.router.DebuggerRoute
import com.copperleaf.ballast.debugger.models.BallastConnectionState
import com.copperleaf.ballast.debugger.models.BallastViewModelState
import com.copperleaf.ballast.navigation.routing.build
import com.copperleaf.ballast.navigation.routing.directions
import com.copperleaf.ballast.navigation.routing.pathParameter

@Composable
fun RowScope.DebuggerPrimaryToolbar(
    currentRoute: DebuggerRoute,
    connections: List<BallastConnectionState?>,
    selectedConnection: BallastConnectionState?,
    viewModels: List<BallastViewModelState?>,
    selectedViewModel: BallastViewModelState?,
    searchText: String,
    postInput: (DebuggerUiContract.Inputs) -> Unit,
) {
    ToolBarActionIconButton(
        imageVector = Icons.Default.ClearAll,
        contentDescription = "Clear All Connections",
        onClick = { postInput(DebuggerUiContract.Inputs.ClearAllConnections) },
    )

    DebuggerConnectionsComboBox(
        connections = connections,
        selectedConnection = selectedConnection,
        postInput = postInput,
    )

    if (selectedConnection != null) {
        DebuggerConnectionViewModelsComboBox(
            currentRoute = currentRoute,
            connection = selectedConnection,
            viewModels = viewModels,
            selectedViewModel = selectedViewModel,
            postInput = postInput,
        )
    }

    OutlinedTextField(
        value = searchText,
        onValueChange = { postInput(DebuggerUiContract.Inputs.UpdateSearchText(it)) },
        modifier = Modifier.weight(1f),
        leadingIcon = { Icon(Icons.Default.Search, "Search") },
        trailingIcon = {
            ToolBarActionIconButton(
                imageVector = Icons.Default.Clear,
                contentDescription = "Clear Search Text",
                onClick = { postInput(DebuggerUiContract.Inputs.UpdateSearchText("")) },
            )
        }
    )
}

@Composable
fun DebuggerConnectionsComboBox(
    connections: List<BallastConnectionState?>,
    selectedConnection: BallastConnectionState?,
    postInput: (DebuggerUiContract.Inputs) -> Unit,
) {
    val currentTime = LocalTimer.current
    DropdownSelector(
        items = listOf(null) + connections,
        value = selectedConnection,
        onValueChange = { connection ->
            if (connection != null) {
                postInput(
                    DebuggerUiContract.Inputs.Navigate(
                        DebuggerRoute.Connection
                            .directions()
                            .pathParameter("connectionId", connection.connectionId)
                            .build()
                    )
                )
            } else {
                postInput(
                    DebuggerUiContract.Inputs.Navigate(
                        DebuggerRoute.Connection
                            .directions()
                            .build()
                    )
                )
            }
        },
        valueRender = {
            if (it != null) {
                if (it.isActive(currentTime)) {
                    "Connection ${it.connectionId}"
                } else {
                    "Connection ${it.connectionId} [OFFLINE]"
                }
            } else {
                "No Connection"
            }
        }
    )
}

@Composable
fun DebuggerConnectionViewModelsComboBox(
    currentRoute: DebuggerRoute,
    connection: BallastConnectionState,
    viewModels: List<BallastViewModelState?>,
    selectedViewModel: BallastViewModelState?,
    postInput: (DebuggerUiContract.Inputs) -> Unit,
) {
    DropdownSelector(
        items = viewModels,
        value = selectedViewModel,
        onValueChange = {
            postInput(
                DebuggerUiContract.Inputs.Navigate(
                    getRouteForSelectedViewModel(
                        currentRoute,
                        connection.connectionId,
                        it?.viewModelName,
                    )
                )
            )
        },
        valueRender = {
            if (it != null) {
                it.viewModelName
            } else {
                "No ViewModel"
            }
        }
    )
}
