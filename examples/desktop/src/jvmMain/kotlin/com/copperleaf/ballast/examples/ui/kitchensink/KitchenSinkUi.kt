package com.copperleaf.ballast.examples.ui.kitchensink

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.ContentAlpha
import androidx.compose.material.Divider
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.ListItem
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ProvideTextStyle
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.copperleaf.ballast.examples.injector.ComposeDesktopInjector

object KitchenSinkUi {

    @Composable
    fun Content(injector: ComposeDesktopInjector, inputStrategySelection: InputStrategySelection) {
        val viewModelCoroutineScope = rememberCoroutineScope()
        val vm = remember(viewModelCoroutineScope, inputStrategySelection) {
            injector.kitchenSinkViewModel(
                viewModelCoroutineScope,
                inputStrategySelection
            )
        }
        val uiState by vm.observeStates().collectAsState()

        Content(uiState) { vm.trySend(it) }
    }

    @Composable
    public fun Content(
        uiState: KitchenSinkContract.State,
        postInput: (KitchenSinkContract.Inputs) -> Unit,
    ) {
        var dropdownExpanded by remember { mutableStateOf(false) }

        Column {
            TopAppBar(
                contentPadding = PaddingValues(0.dp),
                content = {
                    Row(
                        Modifier.fillMaxHeight().padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ProvideTextStyle(value = MaterialTheme.typography.h6) {
                            CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.high) {
                                Text("Kitchen Sink", overflow = TextOverflow.Ellipsis, maxLines = 1)
                            }
                        }
                    }

                    CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                        Row(
                            Modifier.fillMaxHeight().weight(1f),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            ListItem(
                                modifier = Modifier.wrapContentWidth().clickable { dropdownExpanded = true },
                                overlineText = { Text("Input Strategy") },
                                trailing = { Icon(imageVector = Icons.Filled.ArrowDropDown, contentDescription = "") },
                                text = { Text(uiState.inputStrategy.name) },
                            )

                            DropdownMenu(
                                expanded = dropdownExpanded,
                                onDismissRequest = { dropdownExpanded = false }
                            ) {
                                InputStrategySelection.values().forEach { strategy ->
                                    DropdownMenuItem(
                                        onClick = {
                                            dropdownExpanded = false
                                            postInput(KitchenSinkContract.Inputs.ChangeInputStrategy(strategy))
                                        },
                                        content = { Text(strategy.name) },
                                    )
                                }
                            }
                        }
                    }
                }
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                Spacer(Modifier.height(16.dp))
                Text("State", style = MaterialTheme.typography.h5)
                Divider()
                Text("Completed Input: ${uiState.completedInputCounter}")
                Text("Counter: ${uiState.infiniteCounter}")
                Box(Modifier.size(48.dp), contentAlignment = Alignment.Center) {
                    if (uiState.loading) {
                        CircularProgressIndicator()
                    }
                }

                Text("Actions", style = MaterialTheme.typography.h5)
                Divider()

                Text("Inputs", style = MaterialTheme.typography.h6)
                Button(
                    onClick = { postInput(KitchenSinkContract.Inputs.LongRunningInput()) },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("LongRunningInput")
                }
                Button(
                    onClick = { postInput(KitchenSinkContract.Inputs.ErrorRunningInput) },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("ErrorRunningInput")
                }

                Text("Events", style = MaterialTheme.typography.h6)
                Button(
                    onClick = { postInput(KitchenSinkContract.Inputs.LongRunningEvent) },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("LongRunningEvent")
                }
                Button(
                    onClick = { postInput(KitchenSinkContract.Inputs.ErrorRunningEvent) },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("ErrorRunningEvent")
                }
                Button(
                    onClick = { postInput(KitchenSinkContract.Inputs.CloseKitchenSinkWindow) },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("CloseKitchenSinkWindow")
                }

                Text("SideJobs", style = MaterialTheme.typography.h6)
                Button(
                    onClick = { postInput(KitchenSinkContract.Inputs.LongRunningSideJob) },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("LongRunningSideJob")
                }
                Button(
                    onClick = { postInput(KitchenSinkContract.Inputs.ErrorRunningSideJob) },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("ErrorRunningSideJob")
                }
                Button(
                    onClick = { postInput(KitchenSinkContract.Inputs.InfiniteSideJob) },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("InfiniteSideJob")
                }
                Button(
                    onClick = { postInput(KitchenSinkContract.Inputs.CancelInfiniteSideJob) },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("CancelInfiniteSideJob")
                }
            }
        }
    }
}
