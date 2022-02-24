package com.copperleaf.ballast.debugger.windows.sample

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Divider
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ListItem
import androidx.compose.material.MaterialTheme
import androidx.compose.material.RadioButton
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.ApplicationScope
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.rememberWindowState
import com.copperleaf.ballast.InputStrategy
import com.copperleaf.ballast.core.FifoInputStrategy
import com.copperleaf.ballast.core.LifoInputStrategy
import com.copperleaf.ballast.core.ParallelInputStrategy
import com.copperleaf.ballast.debugger.BallastDebuggerClientConnection
import io.ktor.client.engine.cio.CIO
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.plus
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory

class SampleWindow {

    @OptIn(ExperimentalMaterialApi::class)
    @Composable
    fun run(
        applicationScope: ApplicationScope,
        onShowSampleWindowToggled: (Boolean) -> Unit,
    ) = with(applicationScope) {
        Window(
            onCloseRequest = { onShowSampleWindowToggled(false) },
            title = "Sample Window",
            state = rememberWindowState(width = 300.dp, height = 600.dp)
        ) {
            val parentCoroutineScope = rememberCoroutineScope()
            val viewModelCoroutineScope = parentCoroutineScope +
                SupervisorJob(parent = parentCoroutineScope.coroutineContext[Job])

            val logger = LoggerFactory.getLogger(SampleWindow::class.java)
            var inputStrategy: InputStrategy by remember { mutableStateOf(LifoInputStrategy()) }

            val connection = remember(inputStrategy) {
                BallastDebuggerClientConnection(CIO)
            }
            LaunchedEffect(connection) {
                withContext(Dispatchers.IO) {
                    with(connection) { connect() }
                }
            }

            val viewModel = remember(viewModelCoroutineScope, connection, logger, inputStrategy) {
                SampleViewModel(viewModelCoroutineScope, connection, logger, inputStrategy) {
                    onShowSampleWindowToggled(false)
                }
            }

            val uiState by viewModel.observeStates().collectAsState()

            DisposableEffect(viewModel) {
                onDispose { viewModel.onCleared() }
            }

            Surface(Modifier.fillMaxSize()) {
                Column(
                    Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    ui(
                        uiState = uiState,
                        inputStrategy = inputStrategy,
                        updateInputStrategy = { inputStrategy = it },
                        postInput = { viewModel.trySend(it) },
                    )
                }
            }
        }
    }

    @OptIn(ExperimentalMaterialApi::class)
    @Composable
    fun ui(
        uiState: SampleContract.State,
        inputStrategy: InputStrategy,
        updateInputStrategy: (InputStrategy) -> Unit,
        postInput: (SampleContract.Inputs) -> Unit,
    ) {
        Text("Input Strategy", style = MaterialTheme.typography.h5)
        ListItem(
            modifier = Modifier.clickable { updateInputStrategy(LifoInputStrategy()) },
            icon = {
                RadioButton(
                    selected = inputStrategy is LifoInputStrategy,
                    onClick = null,
                )
            },
            text = { Text("Lifo") },
        )
        ListItem(
            modifier = Modifier.clickable { updateInputStrategy(FifoInputStrategy()) },
            icon = {
                RadioButton(
                    selected = inputStrategy is FifoInputStrategy,
                    onClick = null,
                )
            },
            text = { Text("Fifo") },
        )
        ListItem(
            modifier = Modifier.clickable { updateInputStrategy(ParallelInputStrategy()) },
            icon = {
                RadioButton(
                    selected = inputStrategy is ParallelInputStrategy,
                    onClick = null,
                )
            },
            text = { Text("Parallel") },
        )

        Divider()

        Text("Actions", style = MaterialTheme.typography.h5)
        Divider()

        Text("Inputs", style = MaterialTheme.typography.h6)
        Divider()
        Button(
            onClick = { postInput(SampleContract.Inputs.LongRunningInput()) },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("LongRunningInput")
        }
        Button(
            onClick = { postInput(SampleContract.Inputs.ErrorRunningInput) },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("ErrorRunningInput")
        }

        Text("Events", style = MaterialTheme.typography.h6)
        Divider()
        Button(
            onClick = { postInput(SampleContract.Inputs.LongRunningEvent) },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("LongRunningEvent")
        }
        Button(
            onClick = { postInput(SampleContract.Inputs.ErrorRunningEvent) },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("ErrorRunningEvent")
        }

        Text("SideEffects", style = MaterialTheme.typography.h6)
        Divider()
        Button(
            onClick = { postInput(SampleContract.Inputs.LongRunningSideEffect) },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("LongRunningSideEffect")
        }
        Button(
            onClick = { postInput(SampleContract.Inputs.ErrorRunningSideEffect) },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("ErrorRunningSideEffect")
        }
        Button(
            onClick = { postInput(SampleContract.Inputs.InfiniteSideEffect) },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("InfiniteSideEffect")
        }
        Button(
            onClick = { postInput(SampleContract.Inputs.CancelInfiniteSideEffect) },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("CancelInfiniteSideEffect")
        }

        Text("State", style = MaterialTheme.typography.h5)
        Divider()
        Text("${uiState.infiniteCounter}")
        Box(Modifier.size(48.dp), contentAlignment = Alignment.Center) {
            if (uiState.loading) {
                CircularProgressIndicator()
            }
        }
    }
}
