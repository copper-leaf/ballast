package com.copperleaf.ballast.examples.ui.undo

import com.copperleaf.ballast.EventHandler
import com.copperleaf.ballast.EventHandlerScope
import com.copperleaf.ballast.undo.UndoController

class UndoEventHandler(
    private val undoController: UndoController<UndoContract.Inputs, UndoContract.Events, UndoContract.State>,
) : EventHandler<
    UndoContract.Inputs,
    UndoContract.Events,
    UndoContract.State> {
    override suspend fun EventHandlerScope<
        UndoContract.Inputs,
        UndoContract.Events,
        UndoContract.State>.handleEvent(
        event: UndoContract.Events
    ) = when (event) {
        is UndoContract.Events.HandleUndo -> {
            undoController.undo()
        }

        is UndoContract.Events.HandleRedo -> {
            undoController.redo()
        }
    }
}
