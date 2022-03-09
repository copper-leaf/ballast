package com.copperleaf.ballast.debugger.ui.samplecontroller

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ContentAlpha
import androidx.compose.material.Divider
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.ListItem
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ProvideTextStyle
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.copperleaf.ballast.debugger.di.LocalInjector
import com.copperleaf.ballast.debugger.ui.sample.SampleUi

object SampleControllerUi {

    @Composable
    fun run() {
        val injector = LocalInjector.current
        val parentCoroutineScope = rememberCoroutineScope()
        val viewModel = remember(injector, parentCoroutineScope) {
            injector.sampleControllerViewModel(parentCoroutineScope)
        }

        LaunchedEffect(viewModel) {
            viewModel.send(SampleControllerContract.Inputs.Initialize)
        }

        val uiState by viewModel.observeStates().collectAsState()

        Surface(Modifier.fillMaxSize()) {
            Column(
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                ui(
                    uiState = uiState,
                    postInput = { viewModel.trySend(it) },
                )
            }
        }
    }

    @Composable
    fun ui(
        uiState: SampleControllerContract.State,
        postInput: (SampleControllerContract.Inputs) -> Unit,
    ) {
        TopAppBar(
            contentPadding = PaddingValues(0.dp),
            content = {
                Row(
                    Modifier.fillMaxHeight().padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ProvideTextStyle(value = MaterialTheme.typography.h6) {
                        CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.high) {
                            Text("Sample", overflow = TextOverflow.Ellipsis, maxLines = 1)
                        }
                    }
                }

                CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                    Row(
                        Modifier.fillMaxHeight().weight(1f),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        var expanded by remember { mutableStateOf(false) }

                        ListItem(
                            modifier = Modifier.wrapContentWidth().clickable { expanded = !expanded },
                            overlineText = { Text("Input Strategy") },
                            trailing = { Icon(imageVector = Icons.Filled.ArrowDropDown, contentDescription = "") },
                            text = { Text(uiState.inputStrategy.name) },
                        )

                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            InputStrategySelection.values().forEach { strategy ->
                                DropdownMenuItem(
                                    onClick = {
                                        expanded = false
                                        postInput(
                                            SampleControllerContract.Inputs.UpdateInputStrategy(strategy)
                                        )
                                    },
                                    content = { Text(strategy.name) },
                                )
                            }
                        }
                    }
                }
            }
        )

        if (uiState.viewModel != null) {
            Column(Modifier.padding(16.dp)) {
                val updatedViewModel by rememberUpdatedState(uiState.viewModel)

                val sampleUiState by updatedViewModel.observeStates().collectAsState()

                SampleUi.ui(
                    uiState = sampleUiState,
                    postInput = { updatedViewModel.trySend(it) },
                )
            }
            Divider()

            Text(
                "Browse sources for this Sample",
                Modifier.clickable {
                    postInput(SampleControllerContract.Inputs.BrowseSampleSources)
                }
            )

            SelectionContainer() {
                Text(uiState.sampleSourcesUrl, style = MaterialTheme.typography.overline)
            }
        }
    }
}
