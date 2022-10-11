package com.copperleaf.ballast.examples.undo

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import com.copperleaf.ballast.examples.util.Component
import com.copperleaf.ballast.examples.util.ComposeDesktopInjector

class UndoComponent(
    private val injector: ComposeDesktopInjector
) : Component {
    @Composable
    override fun Content() {
        val viewModelCoroutineScope = rememberCoroutineScope()
        val vm = remember(viewModelCoroutineScope) { injector.undoViewModel(viewModelCoroutineScope) }
        val undoController = remember(injector) { injector.undoController }
        val uiState by vm.observeStates().collectAsState()

        UndoComposeUi.Content(undoController, uiState) { vm.trySend(it) }
    }
}
