package com.copperleaf.ballast.debugger.ui

import androidx.compose.foundation.ContextMenuArea
import androidx.compose.foundation.ContextMenuItem
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.material.Divider
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ListItem
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import com.copperleaf.ballast.debugger.DebuggerContract
import com.copperleaf.ballast.debugger.models.BallastConnectionState
import com.copperleaf.ballast.debugger.models.minus
import org.jetbrains.compose.splitpane.ExperimentalSplitPaneApi
import org.jetbrains.compose.splitpane.HorizontalSplitPane
import org.jetbrains.compose.splitpane.rememberSplitPaneState
import kotlin.time.Duration

@OptIn(ExperimentalSplitPaneApi::class, ExperimentalFoundationApi::class, ExperimentalMaterialApi::class)
@Composable
fun ConnectionsList(
    uiState: DebuggerContract.State,
    postInput: (DebuggerContract.Inputs) -> Unit,
) {
    HorizontalSplitPane(
        splitPaneState = rememberSplitPaneState(0.35f)
    ) {
        first(minSize = minSplitPaneSize) {
            Box(Modifier.fillMaxSize()) {
                val scrollState = rememberLazyListState()

                // the list of all Connections
                LazyColumn(Modifier.fillMaxSize(), state = scrollState) {
                    stickyHeader {
                        Column {
                            ContextMenuArea(
                                items = {
                                    buildList<ContextMenuItem> {
                                        this += ContextMenuItem("Clear All Connections") {
                                            postInput(DebuggerContract.Inputs.ClearAll)
                                        }
                                    }
                                }
                            ) {
                                Surface {
                                    ListItem {
                                        Text("Connections")
                                    }
                                    Divider()
                                }
                            }
                        }
                    }

                    items(uiState.applicationState.connections) {
                        ConnectionSummary(uiState, it, postInput)
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
            // the list of ViewModels in the connection, and other state of this Connection
            ConnectionDetails(uiState, uiState.focusedConnection, postInput)
        }
    }
}

@OptIn(ExperimentalMaterialApi::class, ExperimentalFoundationApi::class)
@Composable
fun ConnectionSummary(
    uiState: DebuggerContract.State,
    connectionState: BallastConnectionState,
    postInput: (DebuggerContract.Inputs) -> Unit,
) {
    val currentTime by currentTimeAsState()
    val timeSinceLastSeen: Duration = currentTime - connectionState.lastSeen
    val timeRounded: Duration = timeSinceLastSeen.round()
    val isActive: Boolean = timeSinceLastSeen <= Duration.Companion.seconds(5)

    ContextMenuArea(
        items = {
            buildList<ContextMenuItem> {
                this += ContextMenuItem("Clear") {
                    postInput(
                        DebuggerContract.Inputs.ClearConnection(
                            connectionId = connectionState.connectionId,
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
                        DebuggerContract.Inputs.FocusConnection(
                            connectionId = connectionState.connectionId
                        )
                    )
                }
                .then(
                    if (uiState.focusedConnectionId == connectionState.connectionId) {
                        Modifier.background(MaterialTheme.colors.onSurface.copy(alpha = 0.1f))
                    } else {
                        Modifier
                    }
                ),
            icon = {
                StatusIcon(
                    isActive = isActive,
                    activeText = "Connected",
                    inactiveText = "Disconnected",
                    icon = if (isActive) {
                        Icons.Default.Wifi
                    } else {
                        Icons.Default.WifiOff
                    },
                    modifier = Modifier.wrapContentSize()
                )
            },
            text = {
                Text(
                    text = connectionState.firstSeen.format("MMM dd 'at' hh:mm a"),
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
            overlineText = {
                Text(
                    text = connectionState.connectionId,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            },
        )
    }
}

@Composable
fun ConnectionDetails(
    uiState: DebuggerContract.State,
    connectionState: BallastConnectionState?,
    postInput: (DebuggerContract.Inputs) -> Unit,
) {
    ViewModelList(uiState, connectionState, postInput)
}
