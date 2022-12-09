package com.copperleaf.ballast.examples.ui.undo

import com.copperleaf.ballast.InputHandler
import com.copperleaf.ballast.InputHandlerScope

class UndoInputHandler : InputHandler<
    UndoContract.Inputs,
    UndoContract.Events,
    UndoContract.State> {
    override suspend fun InputHandlerScope<
        UndoContract.Inputs,
        UndoContract.Events,
        UndoContract.State>.handleInput(
        input: UndoContract.Inputs
    ) = when (input) {
        is UndoContract.Inputs.Undo -> {
            postEvent(UndoContract.Events.HandleUndo)
        }

        is UndoContract.Inputs.Redo -> {
            postEvent(UndoContract.Events.HandleRedo)
        }

        is UndoContract.Inputs.UpdateText -> {
            updateState { it.copy(text = input.value) }
        }
    }
}
