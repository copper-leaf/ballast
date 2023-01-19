package com.copperleaf.ballast.examples.ui.undo

import com.copperleaf.ballast.InputHandler
import com.copperleaf.ballast.InputHandlerScope
import com.copperleaf.ballast.undo.state.StateBasedUndoControllerContract

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
            postEvent(
                UndoContract.Events.HandleUndoAction(
                    StateBasedUndoControllerContract.Inputs.Undo()
                )
            )
        }

        is UndoContract.Inputs.Redo -> {
            postEvent(
                UndoContract.Events.HandleUndoAction(
                    StateBasedUndoControllerContract.Inputs.Redo()
                )
            )
        }

        is UndoContract.Inputs.CaptureStateNow -> {
            postEvent(
                UndoContract.Events.HandleUndoAction(
                    StateBasedUndoControllerContract.Inputs.CaptureStateNow()
                )
            )
        }

        is UndoContract.Inputs.UpdateText -> {
            updateState { it.copy(text = input.value) }
        }
    }
}
