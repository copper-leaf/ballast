package com.copperleaf.ballast.debugger.ui.samplecontroller

import com.copperleaf.ballast.InputHandler
import com.copperleaf.ballast.InputHandlerScope
import com.copperleaf.ballast.debugger.di.BallastDebuggerInjector
import com.copperleaf.ballast.debugger.idea.settings.IdeaPluginPrefs
import com.copperleaf.ballast.examples.util.ExamplesContext
import com.copperleaf.ballast.postEventWithState
import com.copperleaf.ballast.postInput

class SampleControllerInputHandler(
    private val injector: BallastDebuggerInjector,
    private val prefs: IdeaPluginPrefs,
) : InputHandler<
    SampleControllerContract.Inputs,
    SampleControllerContract.Events,
    SampleControllerContract.State> {
    override suspend fun InputHandlerScope<
        SampleControllerContract.Inputs,
        SampleControllerContract.Events,
        SampleControllerContract.State>.handleInput(
        input: SampleControllerContract.Inputs
    ) = when (input) {
        is SampleControllerContract.Inputs.Initialize -> {
            updateState { it.copy(sampleSourcesUrl = "${ExamplesContext.repoBaseUrlWithVersion}/${ExamplesContext.sampleSourcesPathInRepo}") }
            postInput(
                SampleControllerContract.Inputs.UpdateInputStrategy(
                    prefs.sampleInputStrategy
                )
            )
        }
        is SampleControllerContract.Inputs.UpdateInputStrategy -> {
            updateState { it.copy(inputStrategy = input.inputStrategy) }

            sideEffect("UpdateInputStrategy") {
                prefs.sampleInputStrategy = input.inputStrategy

                val vm = injector.kitchenSinkViewModel(this, currentStateWhenStarted.inputStrategy.get())

                postInput(SampleControllerContract.Inputs.UpdateViewModel(vm))
            }
        }
        is SampleControllerContract.Inputs.UpdateViewModel -> {
            updateState { it.copy(viewModel = input.viewModel) }
        }
        is SampleControllerContract.Inputs.BrowseSampleSources -> {
            postEventWithState {
                SampleControllerContract.Events.OpenUrlInBrowser(it.sampleSourcesUrl)
            }
        }
    }
}
