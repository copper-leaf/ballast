package com.copperleaf.ballast.debugger.idea.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.copperleaf.ballast.debugger.idea.ui.debugger.widgets.CheckboxArea
import com.copperleaf.ballast.debugger.idea.ui.debugger.widgets.LocalTimer
import com.copperleaf.ballast.debugger.idea.ui.debugger.widgets.ToolBarActionIconButton
import com.copperleaf.ballast.debugger.idea.ui.debugger.widgets.currentTimeAsState
import com.copperleaf.ballast.debugger.idea.ui.settings.injector.SettingsPanelInjector

object SettingsUi {

    @Composable
    fun Content(injector: SettingsPanelInjector) {
        val debuggerUiViewModel = remember(injector) { injector.settingsPanelViewModel }
        val debuggerUiState by debuggerUiViewModel.observeStates().collectAsState()

        val time by currentTimeAsState()

        CompositionLocalProvider(LocalTimer provides time) {
            Content(
                debuggerUiState,
                debuggerUiViewModel::trySend,
            )
        }
    }

    @Composable
    fun Content(
        uiState: SettingsUiContract.State,
        postInput: (SettingsUiContract.Inputs) -> Unit,
    ) {
        Column(
            Modifier
//                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            var debuggerServerPortText by remember(uiState.modifiedSettings.debuggerServerPort) {
                mutableStateOf(uiState.modifiedSettings.debuggerServerPort.toString())
            }
            TextField(
                value = debuggerServerPortText,
                onValueChange = { newValue ->
                    debuggerServerPortText = newValue
                    debuggerServerPortText.toIntOrNull()?.let {
                        postInput(
                            SettingsUiContract.Inputs.UpdateSettings { copy(debuggerServerPort = it) }
                        )
                    }
                },
                label = { Text("Debugger server port") },
                placeholder = { Text("Default: ${uiState.defaultValues.debuggerServerPort}") },
                trailingIcon = {
                    ToolBarActionIconButton(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Debugger Server Port Setting Info",
                        onClick = { },
                    )
                }
            )

            // Auto-Select New Connections
            CheckboxArea(
                checked = uiState.modifiedSettings.autoselectDebuggerConnections,
                onCheckedChange = {
                    postInput(
                        SettingsUiContract.Inputs.UpdateSettings { copy(autoselectDebuggerConnections = it) }
                    )
                },
            ) {
                Text("Auto-select new connections")
            }

            // Special Features
            // - Always Show State
            CheckboxArea(
                checked = uiState.modifiedSettings.alwaysShowCurrentState,
                onCheckedChange = {
                    postInput(
                        SettingsUiContract.Inputs.UpdateSettings { copy(alwaysShowCurrentState = it) }
                    )
                },
            ) {
                Text("Always show current state")
            }

            // - Show Current Route
            CheckboxArea(
                checked = uiState.modifiedSettings.showCurrentRoute,
                onCheckedChange = {
                    postInput(
                        SettingsUiContract.Inputs.UpdateSettings { copy(showCurrentRoute = it) }
                    )
                },
            ) {
                Text("Show current route")
            }
            TextField(
                value = uiState.modifiedSettings.routerViewModelName,
                onValueChange = {
                    postInput(
                        SettingsUiContract.Inputs.UpdateSettings { copy(routerViewModelName = it) }
                    )
                },
                enabled = uiState.modifiedSettings.showCurrentRoute,
                label = { Text("Router ViewModel Name") },
                placeholder = { Text("Default: ${uiState.defaultValues.routerViewModelName}") },
                trailingIcon = {
                    ToolBarActionIconButton(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Router ViewModel Name",
                        onClick = { },
                    )
                }
            )

            Button(
                onClick = {
                    postInput(
                        SettingsUiContract.Inputs.RestoreDefaultSettings
                    )
                },
            ) { Text("Restore Defaults") }
        }
    }
}
