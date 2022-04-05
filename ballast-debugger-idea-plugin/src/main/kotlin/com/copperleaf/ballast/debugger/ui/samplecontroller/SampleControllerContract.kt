package com.copperleaf.ballast.debugger.ui.samplecontroller

import com.copperleaf.ballast.debugger.ui.kitchensink.KitchenSinkViewModel

object SampleControllerContract {
    data class State(
        val sampleSourcesUrl: String = "",
        val inputStrategy: InputStrategySelection = InputStrategySelection.Lifo,
        val viewModel: KitchenSinkViewModel? = null,
    )

    sealed class Inputs {
        object Initialize : Inputs()
        data class UpdateInputStrategy(val inputStrategy: InputStrategySelection) : Inputs()
        object StartViewModel : Inputs()
        data class UpdateViewModel(val viewModel: KitchenSinkViewModel) : Inputs()
        object BrowseSampleSources : Inputs()
    }

    sealed class Events {
        data class OpenUrlInBrowser(val url: String) : Events()
    }
}
