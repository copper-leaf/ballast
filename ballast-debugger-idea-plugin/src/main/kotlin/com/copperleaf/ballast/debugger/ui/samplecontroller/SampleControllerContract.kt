package com.copperleaf.ballast.debugger.ui.samplecontroller

import com.copperleaf.ballast.debugger.ui.sample.SampleViewModel

object SampleControllerContract {
    data class State(
        val sampleSourcesUrl: String = "",
        val inputStrategy: InputStrategySelection = InputStrategySelection.Lifo,
        val viewModel: SampleViewModel? = null,
    )

    sealed class Inputs {
        object Initialize : Inputs()
        data class UpdateInputStrategy(val inputStrategy: InputStrategySelection) : Inputs()
        data class UpdateViewModel(val viewModel: SampleViewModel) : Inputs()
        object BrowseSampleSources : Inputs()
    }

    sealed class Events {
        data class OpenUrlInBrowser(val url: String) : Events()
    }
}
