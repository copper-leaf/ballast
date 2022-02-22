package com.copperleaf.ballast.debugger.ui

import androidx.compose.foundation.ContextMenuArea
import androidx.compose.foundation.ContextMenuItem
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Divider
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ListItem
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.NotificationsPaused
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.copperleaf.ballast.debugger.DebuggerContract
import com.copperleaf.ballast.debugger.models.BallastConnectionState
import com.copperleaf.ballast.debugger.models.BallastDebuggerAction
import com.copperleaf.ballast.debugger.models.BallastViewModelState
import com.copperleaf.ballast.debugger.models.minus
import org.jetbrains.compose.splitpane.ExperimentalSplitPaneApi
import org.jetbrains.compose.splitpane.HorizontalSplitPane
import org.jetbrains.compose.splitpane.rememberSplitPaneState
import kotlin.time.Duration

@OptIn(ExperimentalSplitPaneApi::class, ExperimentalFoundationApi::class, ExperimentalMaterialApi::class)
@Composable
fun ViewModelList(
    uiState: DebuggerContract.State,
    connectionState: BallastConnectionState?,
    postInput: (DebuggerContract.Inputs) -> Unit,
) {
    HorizontalSplitPane(
        splitPaneState = rememberSplitPaneState(0.45f)
    ) {
        first(minSize = minSplitPaneSize) {
            Box(Modifier.fillMaxSize()) {
                val scrollState = rememberLazyListState()

                // the list of all ViewModels in this Connection
                LazyColumn(Modifier.fillMaxSize(), state = scrollState) {
                    stickyHeader {
                        Column {
                            ContextMenuArea(
                                items = {
                                    buildList<ContextMenuItem> {
                                        if (connectionState != null) {
                                            this += ContextMenuItem("Clear All ViewModels") {
                                                postInput(
                                                    DebuggerContract.Inputs.ClearConnection(
                                                        connectionId = connectionState.connectionId
                                                    )
                                                )
                                            }
                                        }
                                    }
                                }
                            ) {
                                Surface {
                                    ListItem {
                                        Text("ViewModels")
                                    }
                                    Divider()
                                }
                            }
                        }
                    }
                    items(connectionState?.viewModels ?: emptyList()) {
                        ViewModelSummary(uiState, it, postInput)
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
        second(minSize = minSplitPaneSize) {
            // the list of Events in the ViewModel, and other state of this ViewModel
            ViewModelDetails(uiState, uiState.focusedViewModel, postInput)
        }
    }
}

@OptIn(ExperimentalMaterialApi::class, ExperimentalFoundationApi::class)
@Composable
fun ViewModelSummary(
    uiState: DebuggerContract.State,
    viewModelState: BallastViewModelState,
    postInput: (DebuggerContract.Inputs) -> Unit,
) {
    val currentTime by currentTimeAsState()
    val timeSinceLastSeen: Duration = currentTime - viewModelState.lastSeen
    val timeRounded = timeSinceLastSeen.round()

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
                Column(Modifier.size(64.dp)) {
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
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            Column(Modifier.weight(1f).padding(1.dp)) {
                                StatusIcon(
                                    isActive = viewModelState.inputInProgress,
                                    activeText = "Processing Input",
                                    inactiveText = "Input Handler Idle",
                                    icon = Icons.Default.Refresh,
                                    modifier = Modifier.fillMaxSize()
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
                                        Icons.Default.NotificationsActive
                                    } else {
                                        Icons.Default.NotificationsPaused
                                    },
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            Column(Modifier.weight(1f).padding(1.dp)) {
                                StatusIcon(
                                    isActive = viewModelState.sideEffectsInProgress,
                                    activeText = "SideEffects active",
                                    inactiveText = "No sideEffects active",
                                    icon = Icons.Default.CloudUpload,
                                    modifier = Modifier.fillMaxSize()
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
                    text = "Last seen $timeRounded ago",
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            },
        )
    }
}

@Composable
fun ViewModelDetails(
    uiState: DebuggerContract.State,
    connectionState: BallastViewModelState?,
    postInput: (DebuggerContract.Inputs) -> Unit,
) {
    EventList(uiState, connectionState, postInput)
}
