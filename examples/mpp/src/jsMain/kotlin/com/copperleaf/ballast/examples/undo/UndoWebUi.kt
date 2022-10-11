package com.copperleaf.ballast.examples.undo

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.copperleaf.ballast.examples.util.ExamplesContext
import com.copperleaf.ballast.examples.util.bulma.BulmaButton
import com.copperleaf.ballast.examples.util.bulma.BulmaButtonGroup
import com.copperleaf.ballast.examples.util.bulma.BulmaInput
import com.copperleaf.ballast.examples.util.bulma.BulmaPanel
import com.copperleaf.ballast.undo.UndoController
import org.jetbrains.compose.web.attributes.ATarget
import org.jetbrains.compose.web.attributes.target
import org.jetbrains.compose.web.dom.A
import org.jetbrains.compose.web.dom.I
import org.jetbrains.compose.web.dom.Text

object UndoWebUi {
    @Composable
    public fun Content(
        undoController: UndoController<UndoContract.Inputs, UndoContract.Events, UndoContract.State>,
        uiState: UndoContract.State,
        postInput: (UndoContract.Inputs) -> Unit,
    ) {
        BulmaPanel(
            headingStart = { Text("Undo") },
            headingEnd = {
                A(
                    href = "${ExamplesContext.samplesUrlWithVersion}/undo",
                    attrs = {
                        title("View sources on GitHub")
                        target(ATarget.Blank)
                    }
                ) {
                    I { Text("Sources") }
                }
            },
        ) {
            BulmaButtonGroup {
                Control {
                    val isUndoAvailable by undoController.isUndoAvailable.collectAsState(false)
                    BulmaButton(
                        onClick = { undoController.undo() },
                        isDisabled = !isUndoAvailable,
                    ) {
                        Text("Undo")
                    }
                }
                Control {
                    val isRedoAvailable by undoController.isRedoAvailable.collectAsState(false)
                    BulmaButton(
                        onClick = { undoController.redo() },
                        isDisabled = !isRedoAvailable,
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
        }
    }
}
