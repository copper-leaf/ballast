package com.copperleaf.ballast.examples.ui.undo

import androidx.fragment.app.Fragment
import com.copperleaf.ballast.EventHandler
import com.copperleaf.ballast.EventHandlerScope
import com.copperleaf.ballast.ExperimentalBallastApi
import com.copperleaf.ballast.examples.router.BallastExamplesRouter
import com.copperleaf.ballast.navigation.routing.RouterContract
import com.copperleaf.ballast.undo.UndoController

@OptIn(ExperimentalBallastApi::class)
class UndoEventHandler(
    private val fragment: Fragment,
    private val router: BallastExamplesRouter,
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
        is UndoContract.Events.GoBack -> {
            router.trySend(RouterContract.Inputs.GoBack())
            Unit
        }

        is UndoContract.Events.HandleUndo -> {
            undoController.undo()
        }

        is UndoContract.Events.HandleRedo -> {
            undoController.redo()
        }
    }
}
