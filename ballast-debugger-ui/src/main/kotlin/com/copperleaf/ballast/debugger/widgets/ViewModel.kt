package com.copperleaf.ballast.debugger.widgets

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ContextMenuArea
import androidx.compose.foundation.ContextMenuItem
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.items
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.ListItem
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.ScrollableTabRow
import androidx.compose.material.Surface
import androidx.compose.material.Tab
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.NotificationsPaused
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material.primarySurface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.copperleaf.ballast.debugger.models.BallastConnectionState
import com.copperleaf.ballast.debugger.models.BallastDebuggerAction
import com.copperleaf.ballast.debugger.models.BallastViewModelState
import com.copperleaf.ballast.debugger.utils.minus
import com.copperleaf.ballast.debugger.utils.removeFraction
import com.copperleaf.ballast.debugger.windows.debugger.DebuggerContract
import org.jetbrains.compose.splitpane.ExperimentalSplitPaneApi
import kotlin.time.Duration
import kotlin.time.DurationUnit

@OptIn(ExperimentalSplitPaneApi::class, ExperimentalFoundationApi::class, ExperimentalMaterialApi::class)
@Composable
fun ViewModelList(
    uiState: DebuggerContract.State,
    connectionState: BallastConnectionState,
    postInput: (DebuggerContract.Inputs) -> Unit,
) {
    SplitPane(
        initialPositionPercentage = 0.35f,
        navigation = {
            // the list of all ViewModels
            stickyHeader {
                Column {
                    ContextMenuArea(
                        items = {
                            buildList<ContextMenuItem> {
                                this += ContextMenuItem("Clear All ViewModels") {
                                    postInput(
                                        DebuggerContract.Inputs.ClearConnection(
                                            connectionId = connectionState.connectionId
                                        )
                                    )
                                }
                            }
                        }
                    ) {
                        TopAppBar(
                            title = { Text("ViewModels", overflow = TextOverflow.Ellipsis, maxLines = 1) }
                        )
                    }
                }
            }
            items(connectionState.viewModels) {
                ViewModelSummary(uiState, connectionState, it, postInput)
            }
        },
        content = {
            // the list of Events in the ViewModel, and other state of this ViewModel
            if (uiState.focusedViewModel != null) {
                ViewModelDetails(uiState, uiState.focusedViewModel, postInput)
            }
        }
    )
}

@OptIn(ExperimentalMaterialApi::class, ExperimentalFoundationApi::class)
@Composable
fun ViewModelSummary(
    uiState: DebuggerContract.State,
    connectionState: BallastConnectionState,
    viewModelState: BallastViewModelState,
    postInput: (DebuggerContract.Inputs) -> Unit,
) {
    val timeSinceLastSeen: Duration = (LocalTimer.current - viewModelState.lastSeen)
        .removeFraction(DurationUnit.SECONDS)

    ContextMenuArea(
        items = {
            buildList<ContextMenuItem> {
                this += ContextMenuItem("Refresh") {
                    postInput(
                        DebuggerContract.Inputs.SendDebuggerAction(
                            BallastDebuggerAction.RequestViewModelRefresh(
                                connectionId = viewModelState.connectionId,
                                viewModelName = viewModelState.viewModelName,
                            )
                        )
                    )
                }
                this += ContextMenuItem("Clear") {
                    postInput(
                        DebuggerContract.Inputs.ClearViewModel(
                            connectionId = viewModelState.connectionId,
                            viewModelName = viewModelState.viewModelName,
                        )
                    )
                }
            }
        }
    ) {
        ListItem(
            modifier = Modifier
                .onHoverState { Modifier.highlight() }
                .clickable {
                    postInput(
                        DebuggerContract.Inputs.FocusViewModel(
                            connectionId = viewModelState.connectionId,
                            viewModelName = viewModelState.viewModelName,
                        )
                    )
                }
                .then(
                    if (uiState.focusedViewModelName == viewModelState.viewModelName) {
                        Modifier.background(MaterialTheme.colors.onSurface.copy(alpha = 0.1f))
                    } else {
                        Modifier
                    }
                ),
            icon = {
                Column(Modifier.size(86.dp)) {
                    if (viewModelState.refreshing) {
                        CircularProgressIndicator(Modifier.size(48.dp))
                    } else {
                        Row(Modifier.weight(1f)) {
                            Column(Modifier.weight(1f).padding(1.dp)) {
                                StatusIcon(
                                    isActive = viewModelState.viewModelActive &&
                                        connectionState.isActive(LocalTimer.current),
                                    activeText = "ViewModel Started",
                                    inactiveText = "ViewModel Cleared",
                                    icon = if (viewModelState.viewModelActive) {
                                        Icons.Default.Wifi
                                    } else {
                                        Icons.Default.WifiOff
                                    },
                                    modifier = Modifier.fillMaxSize(),
                                    count = 0,
                                )
                            }
                            Column(Modifier.weight(1f).padding(1.dp)) {
                                StatusIcon(
                                    isActive = viewModelState.inputInProgress,
                                    activeText = "Processing Input",
                                    inactiveText = "Input Handler Idle",
                                    icon = Icons.Default.Refresh,
                                    modifier = Modifier.fillMaxSize(),
                                    count = viewModelState.runningInputCount,
                                )
                            }
                        }
                        Row(Modifier.weight(1f)) {
                            Column(Modifier.weight(1f).padding(1.dp)) {

                                StatusIcon(
                                    isActive = viewModelState.eventProcessingActive,
                                    activeText = "Event processing active",
                                    inactiveText = "Event processing paused",
                                    icon = if (viewModelState.eventProcessingActive) {
                                        if (viewModelState.eventInProgress) {
                                            Icons.Default.NotificationsActive
                                        } else {
                                            Icons.Default.NotificationsActive
                                        }
                                    } else {
                                        Icons.Default.NotificationsPaused
                                    },
                                    tint = if (viewModelState.eventProcessingActive) {
                                        if (viewModelState.eventInProgress) {
                                            MaterialTheme.colors.secondary
                                        } else {
                                            null
                                        }
                                    } else {
                                        null
                                    },
                                    modifier = Modifier.fillMaxSize(),
                                    count = viewModelState.runningEventCount,
                                )
                            }
                            Column(Modifier.weight(1f).padding(1.dp)) {
                                StatusIcon(
                                    isActive = viewModelState.sideEffectsInProgress,
                                    activeText = "SideEffects active",
                                    inactiveText = "No sideEffects active",
                                    icon = Icons.Default.CloudUpload,
                                    modifier = Modifier.fillMaxSize(),
                                    count = viewModelState.runningSideEffectCount,
                                )
                            }
                        }
                    }
                }
            },
            text = {
                Text(
                    text = viewModelState.viewModelName,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            },
            secondaryText = {
                Text(
                    text = "Last seen $timeSinceLastSeen ago",
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            },
        )
    }
}

enum class ViewModelData(
    val icon: ImageVector,
    val text: String,
) {
    States(Icons.Default.List, "States"),
    Inputs(Icons.Default.Refresh, "Inputs"),
    Events(Icons.Default.NotificationsActive, "Events"),
    SideEffects(Icons.Default.CloudUpload, "SideEffects"),
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ViewModelDetails(
    uiState: DebuggerContract.State,
    viewModelState: BallastViewModelState,
    postInput: (DebuggerContract.Inputs) -> Unit,
) {
    var selectedViewModelData by remember { mutableStateOf(ViewModelData.Inputs) }

    Scaffold(
        topBar = {
            Surface(Modifier.fillMaxWidth(), color = MaterialTheme.colors.primarySurface, elevation = 4.dp) {
                Column(Modifier.fillMaxWidth()) {
                    TopAppBar(
                        title = { Text(viewModelState.viewModelName, overflow = TextOverflow.Ellipsis, maxLines = 1) },
                    )
                    ScrollableTabRow(
                        selectedTabIndex = selectedViewModelData.ordinal,
                        backgroundColor = MaterialTheme.colors.primarySurface,
                    ) {
                        ViewModelData.values().forEach {
                            val selectedContentColor = when (it) {
                                ViewModelData.States -> LocalContentColor.current
                                ViewModelData.Inputs -> if (viewModelState.inputInProgress) {
                                    MaterialTheme.colors.secondary
                                } else {
                                    LocalContentColor.current
                                }
                                ViewModelData.Events -> if (viewModelState.eventInProgress) {
                                    MaterialTheme.colors.secondary
                                } else {
                                    LocalContentColor.current
                                }
                                ViewModelData.SideEffects -> if (viewModelState.sideEffectsInProgress) {
                                    MaterialTheme.colors.secondary
                                } else {
                                    LocalContentColor.current
                                }
                            }

                            Tab(
                                selected = it == selectedViewModelData,
                                onClick = { selectedViewModelData = it },
                                icon = { Icon(it.icon, it.text) },
                                text = { Text(it.text) },
                                selectedContentColor = selectedContentColor,
                            )
                        }
                    }
                }
            }
        }
    ) {
        AnimatedContent(selectedViewModelData) {
            when (it) {
                ViewModelData.Inputs -> {
                    InputList(uiState, viewModelState, postInput)
                }
                ViewModelData.Events -> {
                    EventList(uiState, viewModelState, postInput)
                }
                ViewModelData.States -> {
                    StateSnapshotList(uiState, viewModelState, postInput)
                }
                ViewModelData.SideEffects -> {
                    SideEffectList(uiState, viewModelState, postInput)
                }
            }
        }
    }
}
