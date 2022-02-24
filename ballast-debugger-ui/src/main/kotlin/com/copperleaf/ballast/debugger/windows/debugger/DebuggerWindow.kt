package com.copperleaf.ballast.debugger.windows.debugger

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.ApplicationScope
import androidx.compose.ui.window.MenuBar
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.rememberWindowState
import com.copperleaf.ballast.debugger.widgets.ConnectionsList
import com.copperleaf.ballast.debugger.widgets.LocalTimer
import com.copperleaf.ballast.debugger.widgets.currentTimeAsState
import io.github.copper_leaf.ballast_debugger_ui.BALLAST_VERSION
import org.slf4j.LoggerFactory

class DebuggerWindow {

    @Composable
    fun run(
        applicationScope: ApplicationScope,
        isDarkMode: Boolean,
        onDarkModeToggled: (Boolean) -> Unit,

        showSampleWindow: Boolean,
        onShowSampleWindowToggled: (Boolean) -> Unit,
    ) = with(applicationScope) {
        Window(
            onCloseRequest = ::exitApplication,
            title = "Ballast Debugger ($BALLAST_VERSION)",
            state = rememberWindowState(width = 1200.dp, height = 800.dp)
        ) {
            val coroutineScope = rememberCoroutineScope()
            val logger = LoggerFactory.getLogger(DebuggerWindow::class.java)
            val viewModel = remember(coroutineScope, logger) { DebuggerViewModel(coroutineScope, logger) }

            DisposableEffect(viewModel) {
                viewModel.trySend(DebuggerContract.Inputs.StartServer(8080))

                onDispose { viewModel.onCleared() }
            }

            val uiState by viewModel.observeStates().collectAsState()

            MenuBar {
                Menu("Preferences") {
                    CheckboxItem(
                        text = "Dark Theme",
                        checked = isDarkMode,
                        onCheckedChange = onDarkModeToggled,
                    )
                }
                Menu("Tools") {
                    CheckboxItem(
                        text = "Sample Window",
                        checked = showSampleWindow,
                        onCheckedChange = onShowSampleWindowToggled,
                    )
                }
            }

            val time by currentTimeAsState()

            CompositionLocalProvider(LocalTimer provides time) {
                Surface(Modifier.fillMaxSize()) {
                    Box(Modifier.fillMaxSize()) {
                        ConnectionsList(uiState) { viewModel.trySend(it) }
                    }
                }
            }
        }
    }
}
