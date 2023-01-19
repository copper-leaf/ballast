package com.copperleaf.ballast.examples.ui.undo

import com.copperleaf.ballast.EventHandler
import com.copperleaf.ballast.EventHandlerScope
import com.copperleaf.ballast.undo.state.StateBasedUndoController

class UndoEventHandler(
    private val undoController: StateBasedUndoController<UndoContract.Inputs, UndoContract.Events, UndoContract.State>,
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
        is UndoContract.Events.HandleUndoAction -> {
            undoController.send(event.action)
        }
    }
}
