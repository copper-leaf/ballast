package com.copperleaf.ballast.examples.ui.undo

import com.copperleaf.ballast.undo.state.StateBasedUndoControllerContract

object UndoContract {
    data class State(
        val text: String = ""
    )

    sealed interface Inputs {
        data object Undo : Inputs
        data object Redo : Inputs
        data object CaptureStateNow : Inputs
        data class UpdateText(val value: String) : Inputs
    }

    sealed interface Events {
        class HandleUndoAction(
            val action: StateBasedUndoControllerContract.Inputs<UndoContract.Inputs, UndoContract.Events, UndoContract.State>
        ) : Events
    }
}
