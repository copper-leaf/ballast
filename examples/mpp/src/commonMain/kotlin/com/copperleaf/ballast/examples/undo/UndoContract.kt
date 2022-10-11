package com.copperleaf.ballast.examples.undo

object UndoContract {
    data class State(
        val text: String = ""
    )

    sealed class Inputs {
        data class UpdateText(val value: String) : Inputs()
    }

    sealed class Events {

    }
}
