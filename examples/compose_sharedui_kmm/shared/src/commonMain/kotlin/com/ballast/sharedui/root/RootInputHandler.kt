package com.ballast.sharedui.root

import com.copperleaf.ballast.InputHandler
import com.copperleaf.ballast.InputHandlerScope

class RootInputHandler :
    InputHandler<RootContract.Inputs, RootContract.Events, RootContract.State> {
    override suspend fun InputHandlerScope<RootContract.Inputs, RootContract.Events, RootContract.State>.handleInput(
        input: RootContract.Inputs,
    ) = when (input) {
        is RootContract.Inputs.Increment -> updateState { it.copy(count = it.count + input.amount) }
        is RootContract.Inputs.Decrement -> updateState { it.copy(count = it.count - input.amount) }
    }
}
