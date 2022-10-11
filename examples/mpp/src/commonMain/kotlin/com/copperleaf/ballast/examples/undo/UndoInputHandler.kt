package com.copperleaf.ballast.examples.undo

import com.copperleaf.ballast.InputHandler
import com.copperleaf.ballast.InputHandlerScope

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
        is UndoContract.Inputs.UpdateText -> {
            updateState { it.copy(text = input.value) }
        }
    }
}
