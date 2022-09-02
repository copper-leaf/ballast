package com.copperleaf.ballast.examples.kitchensink.controller

import com.copperleaf.ballast.InputHandler
import com.copperleaf.ballast.InputHandlerScope
import com.copperleaf.ballast.InputStrategy
import com.copperleaf.ballast.examples.kitchensink.KitchenSinkViewModel
import kotlinx.coroutines.CoroutineScope

class KitchenSinkControllerInputHandler(
    private val getNewKitchenSinkViewModel: (CoroutineScope, InputStrategy<*, *, *>)->KitchenSinkViewModel,
) : InputHandler<
    KitchenSinkControllerContract.Inputs,
    KitchenSinkControllerContract.Events,
    KitchenSinkControllerContract.State> {
    override suspend fun InputHandlerScope<
        KitchenSinkControllerContract.Inputs,
        KitchenSinkControllerContract.Events,
        KitchenSinkControllerContract.State>.handleInput(
        input: KitchenSinkControllerContract.Inputs
    ) = when (input) {
        is KitchenSinkControllerContract.Inputs.UpdateInputStrategy -> {
            // clear out the old VM so it doesn't try to display after being cleared
            updateState { it.copy(inputStrategy = input.inputStrategy, viewModel = null) }
            sideJob("StartViewModel") {
                val vm = getNewKitchenSinkViewModel(this, currentStateWhenStarted.inputStrategy.get())
                postInput(KitchenSinkControllerContract.Inputs.UpdateViewModel(vm))
            }
        }
        is KitchenSinkControllerContract.Inputs.UpdateViewModel -> {
            updateState { it.copy(viewModel = input.viewModel) }
        }
    }
}
