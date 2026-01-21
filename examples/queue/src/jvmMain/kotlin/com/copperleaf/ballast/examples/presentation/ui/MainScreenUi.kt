package com.copperleaf.ballast.examples.presentation.ui

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.copperleaf.ballast.examples.di.ComposeDesktopInjector
import com.copperleaf.ballast.examples.presentation.ui.components.JobDropdownMenu
import com.copperleaf.ballast.examples.presentation.ui.components.JobsTableDropdownMenu
import com.copperleaf.ballast.examples.presentation.ui.components.NewJobHeader
import com.copperleaf.ballast.examples.presentation.ui.components.RenderJobsTableCell
import com.copperleaf.ballast.examples.presentation.ui.components.RenderJobsTableCellHeader
import com.copperleaf.ballast.examples.presentation.ui.components.RenderJobsTableCellValue
import com.copperleaf.ballast.examples.presentation.ui.components.columnWidth
import com.copperleaf.ballast.examples.presentation.utils.clockFlow
import com.copperleaf.ballast.examples.presentation.utils.formatted
import eu.wewox.lazytable.LazyTable
import eu.wewox.lazytable.LazyTableItem
import eu.wewox.lazytable.lazyTableDimensions
import eu.wewox.lazytable.lazyTablePinConfiguration
import eu.wewox.lazytable.rememberSaveableLazyTableState
import org.jetbrains.compose.splitpane.ExperimentalSplitPaneApi
import org.jetbrains.compose.splitpane.HorizontalSplitPane
import org.jetbrains.compose.splitpane.rememberSplitPaneState

@OptIn(ExperimentalAnimationApi::class, ExperimentalSplitPaneApi::class, ExperimentalMaterial3Api::class)
object MainScreenUi {

    @Composable
    fun Content(injector: ComposeDesktopInjector) {
        val viewModelCoroutineScope = rememberCoroutineScope()
        val vm = remember(viewModelCoroutineScope, injector) {
            MainScreenViewModel(viewModelCoroutineScope, injector)
        }
        val uiState by remember { vm.observeStates() }.collectAsState()

        Content(injector, uiState) { vm.trySend(it) }
    }

    @Composable
    fun Content(
        injector: ComposeDesktopInjector,
        uiState: MainScreenContract.State,
        postInput: (MainScreenContract.Inputs) -> Unit,
    ) {
        val currentTime by remember(injector) {
            clockFlow(
                injector.clock,
                injector.timezone
            )
        }.collectAsState(injector.clock.now())

        HorizontalSplitPane(
            splitPaneState = rememberSplitPaneState(initialPositionPercentage = 0.80f),
            modifier = Modifier.fillMaxSize()
        ) {
            first {
                Surface(Modifier.fillMaxSize()) {
                    Column(Modifier.fillMaxSize()) {
                        TopAppBar(
                            title = { Text("Ballast Queue Examples") },
                            actions = {
                                Text(currentTime.formatted, Modifier.padding(end = 8.dp))

                                JobsTableDropdownMenu(postInput)
                            }
                        )

                        Column(
                            Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Top,
                            horizontalAlignment = Alignment.Start
                        ) {
                            Text("Queued Jobs", style = MaterialTheme.typography.headlineLarge)

                            NewJobHeader(postInput)
                            Spacer(Modifier.fillMaxWidth().height(8.dp))

                            // display the full list of queued jobs
                            LazyTable(
                                state = rememberSaveableLazyTableState(),
//                                modifier = Modifier.fillMaxWidth().weight(1f),
                                modifier = Modifier.padding(8.dp),
                                pinConfiguration = lazyTablePinConfiguration(columns = 0, rows = 1),
                                dimensions = lazyTableDimensions(
                                    columnSize = { uiState.tableColumns[it].columnWidth },
                                    rowSize = { 48.dp }
                                ),
                            ) {
                                items(
                                    items = uiState.tableCells,
                                    layoutInfo = { LazyTableItem(it.columnIndex, it.rowIndex) }
                                ) {
                                    RenderJobsTableCell(it, injector.json, currentTime, uiState, postInput)
                                }
                            }
                        }
                    }
                }
            }
            second(minSize = 120.dp) {
                Surface(Modifier.fillMaxSize()) {
                    Column(Modifier.fillMaxSize()) {
                        TopAppBar(
                            title = {
                                JobDropdownMenu(uiState.selectedJob, uiState.selectedJob != null, postInput)
                            },
                            actions = {
                            }
                        )

                        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                            Text("Job Details", style = MaterialTheme.typography.headlineLarge)

                            if (uiState.selectedJob != null) {
                                LazyColumn(
                                    modifier = Modifier.fillMaxWidth().weight(1f),
                                ) {
                                    items(uiState.detailColumns) { column ->
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center,
                                            modifier = Modifier
                                                .background(MaterialTheme.colorScheme.surface)
                                                .border(Dp.Hairline, MaterialTheme.colorScheme.onSurface)
                                        ) {
                                            RenderJobsTableCellHeader(column, uiState, postInput)
                                            HorizontalDivider()
                                            RenderJobsTableCellValue(
                                                uiState.selectedJob,
                                                column,
                                                injector.json,
                                                currentTime,
                                                uiState,
                                                postInput,
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
