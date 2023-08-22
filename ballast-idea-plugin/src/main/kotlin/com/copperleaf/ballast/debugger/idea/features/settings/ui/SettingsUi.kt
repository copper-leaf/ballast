package com.copperleaf.ballast.debugger.idea.features.settings.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.copperleaf.ballast.debugger.idea.features.debugger.ui.widgets.CheckboxArea
import com.copperleaf.ballast.debugger.idea.features.debugger.ui.widgets.ProvideTime
import com.copperleaf.ballast.debugger.idea.features.debugger.ui.widgets.RadioGroup
import com.copperleaf.ballast.debugger.idea.features.debugger.ui.widgets.Section
import com.copperleaf.ballast.debugger.idea.features.debugger.ui.widgets.ToolBarActionIconButton
import com.copperleaf.ballast.debugger.idea.features.settings.injector.SettingsPanelInjector
import com.copperleaf.ballast.debugger.idea.features.settings.vm.SettingsUiContract
import com.copperleaf.ballast.debugger.idea.features.templates.BallastViewModel
import com.copperleaf.ballast.debugger.idea.theme.IdeaPluginTheme

object SettingsUi {

    @Composable
    fun Content(injector: SettingsPanelInjector) {
        val debuggerUiViewModel = remember(injector) { injector.settingsPanelViewModel }
        val debuggerUiState by debuggerUiViewModel.observeStates().collectAsState()

        IdeaPluginTheme(injector.project) {
            ProvideTime {
                Content(
                    debuggerUiState,
                    debuggerUiViewModel::trySend,
                )
            }
        }
    }

    @Composable
    fun Content(
        uiState: SettingsUiContract.State,
        postInput: (SettingsUiContract.Inputs) -> Unit,
    ) {
        Column(
            Modifier
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Section(
                title = { Text("General") }
            ) {
                CheckboxArea(
                    checked = uiState.modifiedSettings.darkTheme,
                    onCheckedChange = {
                        postInput(
                            SettingsUiContract.Inputs.UpdateSettings { copy(darkTheme = it) }
                        )
                    },
                ) {
                    Text("Dark Theme")
                }
            }

            Section(
                title = { Text("Debugger Server") }
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
            }

            Section(
                title = { Text("Debugger UI") }
            ) {
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
            }

            Section(
                title = { Text("Templates") }
            ) {
                RadioGroup(
                    items = BallastViewModel.ViewModelTemplate.values().toList(),
                    value = uiState.modifiedSettings.baseViewModelType,
                    onValueChange = {
                        postInput(
                            SettingsUiContract.Inputs.UpdateSettings { copy(baseViewModelType = it) }
                        )
                    },
                    valueRender = { it.displayName },
                    label = { Text("Base ViewModel Type") }
                )

                RadioGroup(
                    items = BallastViewModel.DefaultVisibility.values().toList(),
                    value = uiState.modifiedSettings.defaultVisibility,
                    onValueChange = {
                        postInput(
                            SettingsUiContract.Inputs.UpdateSettings { copy(defaultVisibility = it) }
                        )
                    },
                    valueRender = { it.displayName },
                    label = { Text("Visibility") }
                )

                CheckboxArea(
                    checked = uiState.modifiedSettings.allComponentsIncludesViewModel,
                    onCheckedChange = {
                        postInput(
                            SettingsUiContract.Inputs.UpdateSettings { copy(allComponentsIncludesViewModel = it) }
                        )
                    },
                ) {
                    Text("All Components - Include ViewModel")
                }

                CheckboxArea(
                    checked = uiState.modifiedSettings.allComponentsIncludesSavedStateAdapter,
                    onCheckedChange = {
                        postInput(
                            SettingsUiContract.Inputs.UpdateSettings { copy(allComponentsIncludesSavedStateAdapter = it) }
                        )
                    },
                ) {
                    Text("All Components - Include SavedStateAdapter")
                }

                CheckboxArea(
                    checked = uiState.modifiedSettings.useDataObjects,
                    onCheckedChange = {
                        postInput(
                            SettingsUiContract.Inputs.UpdateSettings { copy(useDataObjects = it) }
                        )
                    },
                ) {
                    Text("Use Data Objects for Inputs and Events")
                }
            }

            Divider()

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.End),
            ) {
                Button(
                    onClick = {
                        postInput(
                            SettingsUiContract.Inputs.DiscardChanges
                        )
                    },
                    enabled = uiState.isModified,
                ) { Text("Discard Changes") }
                Button(
                    onClick = {
                        postInput(
                            SettingsUiContract.Inputs.RestoreDefaultSettings
                        )
                    },
                    enabled = uiState.isModified,
                ) { Text("Restore Defaults") }
                Button(
                    onClick = {
                        postInput(
                            SettingsUiContract.Inputs.ApplySettings
                        )
                    },
                    enabled = uiState.isModified,
                ) { Text("Save All") }
            }
        }
    }
}
