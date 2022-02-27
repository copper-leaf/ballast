package com.copperleaf.ballast.debugger.ui.samplecontroller

import com.copperleaf.ballast.InputHandler
import com.copperleaf.ballast.InputHandlerScope
import com.copperleaf.ballast.debugger.BallastDebuggerClientConnection
import com.copperleaf.ballast.debugger.di.BallastDebuggerInjector
import com.copperleaf.ballast.debugger.idea.settings.IdeaPluginPrefs
import com.copperleaf.ballast.postEventWithState
import com.copperleaf.ballast.postInput
import com.intellij.openapi.diagnostic.Logger
import io.ktor.client.engine.cio.CIO
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

class SampleControllerInputHandler
@Inject
constructor(
    private val injector: BallastDebuggerInjector,
    private val logger: Logger,
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
            updateState { it.copy(sampleSourcesUrl = "${injector.repoBaseUrl}/${injector.sampleSourcesPathInRepo}") }
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
                val connection = BallastDebuggerClientConnection(CIO)
                launch(Dispatchers.IO) {
                    with(connection) { connect() }
                }

                val vm = injector.sampleViewModel(this, connection, currentStateWhenStarted.inputStrategy.get())

                postInput(SampleControllerContract.Inputs.UpdateViewModel(vm))
            }
        }
        is SampleControllerContract.Inputs.UpdateViewModel -> {
            val previousState = getAndUpdateState { it.copy(viewModel = input.viewModel) }

            // if we had a VM already, clear it out when setting a new one
            previousState.viewModel?.onCleared()

            Unit
        }
        is SampleControllerContract.Inputs.BrowseSampleSources -> {
            postEventWithState {
                SampleControllerContract.Events.OpenUrlInBrowser(it.sampleSourcesUrl)
            }
        }
    }
}
