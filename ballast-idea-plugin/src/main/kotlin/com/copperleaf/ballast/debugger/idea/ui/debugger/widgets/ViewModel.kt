@file:Suppress("UNUSED_PARAMETER")
package com.copperleaf.ballast.debugger.idea.ui.debugger.widgets

import androidx.compose.foundation.ContextMenuArea
import androidx.compose.foundation.ContextMenuItem
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.items
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.ListItem
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.NotificationsPaused
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.copperleaf.ballast.debugger.idea.ui.debugger.DebuggerUiContract
import com.copperleaf.ballast.debugger.models.BallastConnectionState
import com.copperleaf.ballast.debugger.models.BallastViewModelState
import com.copperleaf.ballast.debugger.utils.minus
import com.copperleaf.ballast.debugger.utils.removeFraction
import com.copperleaf.ballast.debugger.versions.v3.BallastDebuggerActionV3
import kotlin.time.Duration
import kotlin.time.DurationUnit

@Composable
fun ViewModelList(
    uiState: DebuggerUiContract.State,
    connectionState: BallastConnectionState,
    postInput: (DebuggerUiContract.Inputs) -> Unit,
) {
    SplitPane(
        splitPaneState = uiState.viewModelsPanePercentage,
        navigation = {
            // the list of all ViewModels
            stickyHeader {
                Column {
                    ContextMenuArea(
                        items = {
                            buildList<ContextMenuItem> {
                                this += ContextMenuItem("Clear All ViewModels") {
                                    postInput(
                                        DebuggerUiContract.Inputs.ClearConnection(
                                            connectionId = connectionState.connectionId
                                        )
                                    )
                                }
                            }
                        }
                    ) {
                        TopAppBar(
                            title = {
                                Column {
                                    Text(
                                        text = connectionState.connectionId,
                                        style = MaterialTheme.typography.overline,
                                        color = LocalContentColor.current,
                                    )
                                    Text("ViewModels", overflow = TextOverflow.Ellipsis, maxLines = 1)
                                }
                            }
                        )
                    }
                }
            }
            items(connectionState.viewModels) {
                ViewModelSummary(uiState, it, postInput)
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

@Composable
fun ViewModelSummary(
    uiState: DebuggerUiContract.State,
    viewModelState: BallastViewModelState,
    postInput: (DebuggerUiContract.Inputs) -> Unit,
) {
    val timeSinceLastSeen: Duration = (LocalTimer.current - viewModelState.lastSeen)
        .removeFraction(DurationUnit.SECONDS)

    ContextMenuArea(
        items = {
            buildList<ContextMenuItem> {
                this += ContextMenuItem("Refresh") {
                    postInput(
                        DebuggerUiContract.Inputs.SendDebuggerAction(
                            BallastDebuggerActionV3.RequestViewModelRefresh(
                                connectionId = viewModelState.connectionId,
                                viewModelName = viewModelState.viewModelName,
                            )
                        )
                    )
                }
                this += ContextMenuItem("Clear") {
                    postInput(
                        DebuggerUiContract.Inputs.ClearViewModel(
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
                        DebuggerUiContract.Inputs.FocusViewModel(
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
                                    isActive = viewModelState.viewModelActive,
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
                                    isActive = viewModelState.sideJobsInProgress,
                                    activeText = "Side Jobs active",
                                    inactiveText = "No Side Jobs active",
                                    icon = Icons.Default.CloudUpload,
                                    modifier = Modifier.fillMaxSize(),
                                    count = viewModelState.runningSideJobCount,
                                )
                            }
                        }
                    }
                }
            },
            overlineText = {
                Text(
                    text = viewModelState.viewModelType,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
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

@Composable
fun ViewModelDetails(
    uiState: DebuggerUiContract.State,
    viewModelState: BallastViewModelState,
    postInput: (DebuggerUiContract.Inputs) -> Unit,
) {
//    Scaffold(
//        topBar = {
//            Surface(Modifier.fillMaxWidth(), color = MaterialTheme.colors.primarySurface, elevation = 4.dp) {
//                Column(Modifier.fillMaxWidth()) {
//                    TopAppBar(
//                        title = { Text(viewModelState.viewModelName, overflow = TextOverflow.Ellipsis, maxLines = 1) },
//                    )
//                    ScrollableTabRow(
//                        selectedTabIndex = uiState.selectedViewModelContentTab!!.ordinal,
//                        backgroundColor = MaterialTheme.colors.primarySurface,
//                    ) {
//                        ViewModelContentTab.values().forEach {
//                            val selectedContentColor = when (it) {
//                                ViewModelContentTab.States -> LocalContentColor.current
//                                ViewModelContentTab.Inputs -> if (viewModelState.inputInProgress) {
//                                    MaterialTheme.colors.secondary
//                                } else {
//                                    LocalContentColor.current
//                                }
//                                ViewModelContentTab.Events -> if (viewModelState.eventInProgress) {
//                                    MaterialTheme.colors.secondary
//                                } else {
//                                    LocalContentColor.current
//                                }
//                                ViewModelContentTab.SideJobs -> if (viewModelState.sideJobsInProgress) {
//                                    MaterialTheme.colors.secondary
//                                } else {
//                                    LocalContentColor.current
//                                }
//                            }
//
//                            Tab(
//                                selected = it == uiState.selectedViewModelContentTab,
//                                onClick = { postInput(DebuggerUiContract.Inputs.UpdateSelectedViewModelContentTab(it)) },
//                                icon = { Icon(it.icon, it.text) },
//                                text = { Text(it.text) },
//                                selectedContentColor = selectedContentColor,
//                            )
//                        }
//                    }
//                }
//            }
//        }
//    ) {
//        AnimatedContent(uiState.selectedViewModelContentTab) {
//            when (it) {
//                ViewModelContentTab.Inputs -> {
//                    InputList(uiState, viewModelState, postInput)
//                }
//                ViewModelContentTab.Events -> {
//                    EventList(uiState, viewModelState, postInput)
//                }
//                ViewModelContentTab.States -> {
//                    StateSnapshotList(uiState, viewModelState, postInput)
//                }
//                ViewModelContentTab.SideJobs -> {
//                    SideJobList(uiState, viewModelState, postInput)
//                }
//                else -> { }
//            }
//        }
//    }
}
