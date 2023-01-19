package com.copperleaf.ballast.undo.state

import com.copperleaf.ballast.EventHandler
import com.copperleaf.ballast.EventHandlerScope
import com.copperleaf.ballast.undo.UndoScope

internal class StateBasedUndoControllerEventHandler<Inputs : Any, Events : Any, State : Any>(
    private val undoScope: UndoScope<Inputs, Events, State>
) : EventHandler<
        StateBasedUndoControllerContract.Inputs<Inputs, Events, State>,
        StateBasedUndoControllerContract.Events<Inputs, Events, State>,
        StateBasedUndoControllerContract.State<Inputs, Events, State>> {
    override suspend fun EventHandlerScope<
            StateBasedUndoControllerContract.Inputs<Inputs, Events, State>,
            StateBasedUndoControllerContract.Events<Inputs, Events, State>,
            StateBasedUndoControllerContract.State<Inputs, Events, State>>.handleEvent(
        event: StateBasedUndoControllerContract.Events<Inputs, Events, State>,
    ) = when (event) {
        is StateBasedUndoControllerContract.Events.RestoreState -> {
            undoScope.restoreState(event.stateToRestore)
        }
    }
}
