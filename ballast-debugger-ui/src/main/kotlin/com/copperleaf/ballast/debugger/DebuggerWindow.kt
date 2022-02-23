package com.copperleaf.ballast.debugger

import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.ApplicationScope
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.rememberWindowState
import com.copperleaf.ballast.debugger.ui.ConnectionsList
import io.github.copper_leaf.ballast_debugger_ui.BALLAST_VERSION
import org.slf4j.LoggerFactory

class DebuggerWindow {

    @Composable
    fun run(applicationScope: ApplicationScope) = with(applicationScope) {
        Window(
            onCloseRequest = ::exitApplication,
            title = "Ballast Debugger ($BALLAST_VERSION)",
            state = rememberWindowState(width = 1200.dp, height = 800.dp)
        ) {
            val coroutineScope = rememberCoroutineScope()
            val logger = LoggerFactory.getLogger(DebuggerWindow::class.java)
            val viewModel = remember(coroutineScope, logger) { DebuggerViewModel(coroutineScope, logger) }

            LaunchedEffect(viewModel) {
                viewModel.send(DebuggerContract.Inputs.StartServer(8080))
            }

            val uiState by viewModel.observeStates().collectAsState()

            MaterialTheme {
                ConnectionsList(uiState) { viewModel.trySend(it) }
            }
        }
    }
}
