package com.copperleaf.ballast.examples.ui.undo

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Redo
import androidx.compose.material.icons.filled.SaveAlt
import androidx.compose.material.icons.filled.Undo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.copperleaf.ballast.examples.injector.ComposeDesktopInjector
import com.copperleaf.ballast.undo.state.StateBasedUndoController

object UndoUi {

    @Composable
    fun Content(injector: ComposeDesktopInjector) {
        val viewModelCoroutineScope = rememberCoroutineScope()
        val undoController: StateBasedUndoController<
                UndoContract.Inputs,
                UndoContract.Events,
                UndoContract.State> = remember(injector) { StateBasedUndoController() }
        val vm = remember(viewModelCoroutineScope, undoController) {
            injector.undoViewModel(
                viewModelCoroutineScope,
                undoController
            )
        }
        val uiState by vm.observeStates().collectAsState()

        val isUndoEnabled by undoController.isUndoAvailable.collectAsState(false)
        val isRedoEnabled by undoController.isRedoAvailable.collectAsState(false)

        Content(
            isUndoEnabled,
            isRedoEnabled,
            uiState,
        ) { vm.trySend(it) }
    }

    @Composable
    public fun Content(
        isUndoEnabled: Boolean,
        isRedoEnabled: Boolean,
        uiState: UndoContract.State,
        postInput: (UndoContract.Inputs) -> Unit,
    ) {
        Column(Modifier.fillMaxSize()) {
            TopAppBar(title = { Text("Undo/Redo") })
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Button(
                        onClick = { postInput(UndoContract.Inputs.Undo) },
                        enabled = isUndoEnabled,
                    ) {
                        Icon(Icons.Default.Undo, "Undo")
                        Text("Undo")
                    }
                    Button(
                        onClick = { postInput(UndoContract.Inputs.Redo) },
                        enabled = isRedoEnabled,
                    ) {
                        Icon(Icons.Default.Redo, "Redo")
                        Text("Redo")
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    TextField(
                        value = uiState.text,
                        onValueChange = {
                            postInput(UndoContract.Inputs.UpdateText(it))
                        }
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Button(
                        onClick = { postInput(UndoContract.Inputs.CaptureStateNow) },
                    ) {
                        Icon(Icons.Default.SaveAlt, "Capture State")
                        Text("Capture State")
                    }
                }
            }
        }
    }
}
