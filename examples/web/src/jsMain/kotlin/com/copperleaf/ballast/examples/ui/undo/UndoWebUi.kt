package com.copperleaf.ballast.examples.ui.undo

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import com.copperleaf.ballast.examples.injector.ComposeWebInjector
import com.copperleaf.ballast.examples.ui.util.bulma.BulmaButton
import com.copperleaf.ballast.examples.ui.util.bulma.BulmaButtonGroup
import com.copperleaf.ballast.examples.ui.util.bulma.BulmaInput
import com.copperleaf.ballast.undo.state.StateBasedUndoController
import org.jetbrains.compose.web.dom.Text

object UndoWebUi {

    @Composable
    fun Content(injector: ComposeWebInjector) {
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
        BulmaButtonGroup {
            Control {
                BulmaButton(
                    onClick = { postInput(UndoContract.Inputs.Undo) },
                    isDisabled = !isUndoEnabled,
                ) {
                    Text("Undo")
                }
            }
            Control {
                BulmaButton(
                    onClick = { postInput(UndoContract.Inputs.Redo) },
                    isDisabled = !isRedoEnabled,
                ) {
                    Text("Redo")
                }
            }
        }
        BulmaInput(
            "Text Input",
            value = uiState.text,
            onValueChange = { postInput(UndoContract.Inputs.UpdateText(it)) }
        )
        BulmaButtonGroup {
            Control {
                BulmaButton(
                    onClick = { postInput(UndoContract.Inputs.CaptureStateNow) },
                ) {
                    Text("Capture State")
                }
            }
        }
    }
}
