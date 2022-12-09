package com.copperleaf.ballast.examples.ui.undo

object UndoContract {
    data class State(
        val text: String = ""
    )

    sealed class Inputs {
        object Undo : Inputs()
        object Redo : Inputs()
        data class UpdateText(val value: String) : Inputs()
    }

    sealed class Events {
        object HandleUndo : Events()
        object HandleRedo : Events()
    }
}
