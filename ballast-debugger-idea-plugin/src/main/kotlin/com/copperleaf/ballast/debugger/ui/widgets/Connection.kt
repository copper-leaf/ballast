package com.copperleaf.ballast.debugger.ui.widgets

import androidx.compose.foundation.ContextMenuArea
import androidx.compose.foundation.ContextMenuItem
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ListItem
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import com.copperleaf.ballast.debugger.models.BallastConnectionState
import com.copperleaf.ballast.debugger.ui.debugger.DebuggerContract
import com.copperleaf.ballast.debugger.utils.minus
import com.copperleaf.ballast.debugger.utils.removeFraction
import io.github.copper_leaf.ballast_debugger_idea_plugin.BALLAST_VERSION
import kotlin.time.Duration
import kotlin.time.DurationUnit

@Composable
fun ConnectionsList(
    uiState: DebuggerContract.State,
    postInput: (DebuggerContract.Inputs) -> Unit,
) {
    SplitPane(
        splitPaneState = uiState.connectionsPanePercentage,
        navigation = {
            // the list of all Connections
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
                        TopAppBar(
                            title = { Text("Connections", overflow = TextOverflow.Ellipsis, maxLines = 1) }
                        )
                    }
                }
            }

            items(uiState.applicationState.connections) {
                ConnectionSummary(uiState, it, postInput)
            }
        },
        content = {
            // the list of ViewModels in the connection, and other state of this Connection

            if (uiState.focusedConnection != null) {
                ViewModelList(uiState, uiState.focusedConnection, postInput)
            }
        }
    )
}

@Composable
fun ConnectionSummary(
    uiState: DebuggerContract.State,
    connectionState: BallastConnectionState,
    postInput: (DebuggerContract.Inputs) -> Unit,
) {
    val timeRounded: Duration = (LocalTimer.current - connectionState.lastSeen).removeFraction(DurationUnit.SECONDS)
    val isActive: Boolean = connectionState.isActive(LocalTimer.current)

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
                .onHoverState { Modifier.highlight() }
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
                    modifier = Modifier.wrapContentSize(),
                    count = 0,
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
                    text = "${connectionState.connectionId} (${connectionState.connectionBallastVersion})",
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = if (connectionState.connectionBallastVersion == BALLAST_VERSION) {
                        LocalContentColor.current
                    } else {
                        MaterialTheme.colors.error
                    }
                )
            },
        )
    }
}
