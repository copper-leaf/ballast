package com.copperleaf.ballast.examples.ui.undo

import com.copperleaf.ballast.undo.state.StateBasedUndoControllerContract

object UndoContract {
    data class State(
        val text: String = ""
    )

    sealed class Inputs {
        object Undo : Inputs()
        object Redo : Inputs()
        object CaptureStateNow : Inputs()
        data class UpdateText(val value: String) : Inputs()
    }

    sealed class Events {
        class HandleUndoAction(
            val action: StateBasedUndoControllerContract.Inputs<UndoContract.Inputs, UndoContract.Events, UndoContract.State>
        ) : Events()
    }
}
