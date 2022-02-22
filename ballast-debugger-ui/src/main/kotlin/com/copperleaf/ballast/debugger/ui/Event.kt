package com.copperleaf.ballast.debugger.ui

import androidx.compose.foundation.ContextMenuArea
import androidx.compose.foundation.ContextMenuItem
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.material.Divider
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ListItem
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.copperleaf.ballast.debugger.DebuggerContract
import com.copperleaf.ballast.debugger.models.BallastDebuggerEvent
import com.copperleaf.ballast.debugger.models.BallastViewModelState
import org.jetbrains.compose.splitpane.ExperimentalSplitPaneApi
import org.jetbrains.compose.splitpane.HorizontalSplitPane
import org.jetbrains.compose.splitpane.rememberSplitPaneState

@OptIn(ExperimentalSplitPaneApi::class, ExperimentalFoundationApi::class, ExperimentalMaterialApi::class)
@Composable
fun EventList(
    uiState: DebuggerContract.State,
    viewModelState: BallastViewModelState?,
    postInput: (DebuggerContract.Inputs) -> Unit,
) {
    HorizontalSplitPane(
        splitPaneState = rememberSplitPaneState(1.0f)
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
                                        if (viewModelState != null) {
                                            this += ContextMenuItem("Clear All Events") {
                                                postInput(
                                                    DebuggerContract.Inputs.ClearViewModel(
                                                        connectionId = viewModelState.connectionId,
                                                        viewModelName = viewModelState.viewModelName,
                                                    )
                                                )
                                            }
                                        }
                                    }
                                }
                            ) {
                                Surface {
                                    ListItem {
                                        Text("Events")
                                    }
                                    Divider()
                                }
                            }
                        }
                    }
                    items(viewModelState?.inputs ?: emptyList()) {
                        EventSummary(uiState, it, postInput)
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
            EventDetails(uiState, uiState.focusedEvent, postInput)
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun EventSummary(
    uiState: DebuggerContract.State,
    eventState: String,
    postInput: (DebuggerContract.Inputs) -> Unit,
) {
    ListItem(
        modifier = Modifier
            .clickable {
            }
    ) {
        Text(eventState)
    }
}

@Composable
fun EventDetails(
    uiState: DebuggerContract.State,
    eventState: BallastDebuggerEvent?,
    postInput: (DebuggerContract.Inputs) -> Unit,
) {
    Text(eventState?.toString() ?: "")
}
