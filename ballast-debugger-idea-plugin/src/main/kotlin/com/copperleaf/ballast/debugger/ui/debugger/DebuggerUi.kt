package com.copperleaf.ballast.debugger.ui.debugger

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import com.copperleaf.ballast.debugger.di.LocalInjector
import com.copperleaf.ballast.debugger.ui.widgets.ConnectionsList
import com.copperleaf.ballast.debugger.ui.widgets.LocalTimer
import com.copperleaf.ballast.debugger.ui.widgets.currentTimeAsState

object DebuggerUi {

    @Composable
    fun run() {
        val injector = LocalInjector.current
        val parentCoroutineScope = rememberCoroutineScope()
        val viewModel = remember(injector, parentCoroutineScope) {
            injector.debuggerViewModel(parentCoroutineScope)
        }

        LaunchedEffect(viewModel) {
            viewModel.send(DebuggerContract.Inputs.StartServer(9684))
        }

        val uiState by viewModel.observeStates().collectAsState()

        ui(uiState, viewModel::trySend)
    }

    @Composable
    fun ui(
        uiState: DebuggerContract.State,
        postInput: (DebuggerContract.Inputs) -> Unit,
    ) {
        val time by currentTimeAsState()

        CompositionLocalProvider(LocalTimer provides time) {
            Surface(Modifier.fillMaxSize()) {
                Box(Modifier.fillMaxSize()) {
                    ConnectionsList(uiState, postInput)
                }
            }
        }
    }
}
