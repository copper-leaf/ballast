package com.copperleaf.ballast.examples.kitchensink.controller

import com.copperleaf.ballast.examples.kitchensink.KitchenSinkViewModel

object KitchenSinkControllerContract {
    data class State(
        val inputStrategy: InputStrategySelection = InputStrategySelection.Lifo,
        val viewModel: KitchenSinkViewModel? = null,
    )

    sealed class Inputs {
        data class UpdateInputStrategy(val inputStrategy: InputStrategySelection) : Inputs()
        data class UpdateViewModel(val viewModel: KitchenSinkViewModel) : Inputs()
        object GoBack : Inputs()
    }

    sealed class Events {
        object NavigateBackwards : Events()
    }
}
