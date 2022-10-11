package com.copperleaf.ballast.examples.undo

import com.copperleaf.ballast.EventHandler
import com.copperleaf.ballast.EventHandlerScope

class UndoEventHandler : EventHandler<
    UndoContract.Inputs,
    UndoContract.Events,
    UndoContract.State> {
    override suspend fun EventHandlerScope<
        UndoContract.Inputs,
        UndoContract.Events,
        UndoContract.State>.handleEvent(
        event: UndoContract.Events
    ) {
    }
}
