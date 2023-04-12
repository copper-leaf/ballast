package com.copperleaf.ballast.examples.ui.kitchensink

import com.copperleaf.ballast.InputHandler
import com.copperleaf.ballast.InputHandlerScope
import com.copperleaf.ballast.core.KillSwitch
import com.copperleaf.ballast.examples.router.BallastExamples
import com.copperleaf.ballast.navigation.routing.build
import com.copperleaf.ballast.navigation.routing.directions
import com.copperleaf.ballast.navigation.routing.queryParameter
import com.copperleaf.ballast.observeFlows
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow

class KitchenSinkInputHandler(
    private val killSwitch: KillSwitch<
            KitchenSinkContract.Inputs,
            KitchenSinkContract.Events,
            KitchenSinkContract.State>,
) : InputHandler<
        KitchenSinkContract.Inputs,
        KitchenSinkContract.Events,
        KitchenSinkContract.State> {

    override suspend fun InputHandlerScope<
            KitchenSinkContract.Inputs,
            KitchenSinkContract.Events,
            KitchenSinkContract.State>.handleInput(
        input: KitchenSinkContract.Inputs
    ) = when (input) {
        is KitchenSinkContract.Inputs.CloseKitchenSinkWindow -> {
            postEvent(KitchenSinkContract.Events.CloseWindow)
        }

        is KitchenSinkContract.Inputs.ChangeInputStrategy -> {
            postEvent(
                KitchenSinkContract.Events.NavigateTo(
                    BallastExamples.KitchenSink
                        .directions()
                        .queryParameter("inputStrategy", input.inputStrategy.name)
                        .build()
                )
            )
        }

        is KitchenSinkContract.Inputs.LongRunningInput -> {
            updateState { it.copy(loading = true) }
            delay(5000)
            updateState { it.copy(loading = false, completedInputCounter = it.completedInputCounter + 1) }
        }

        is KitchenSinkContract.Inputs.LongRunningEvent -> {
            postEvent(KitchenSinkContract.Events.LongRunningEvent())
        }

        is KitchenSinkContract.Inputs.LongRunningSideJob -> {
            sideJob("LongRunningSideJob") {
                delay(5000)
            }
        }

        is KitchenSinkContract.Inputs.InfiniteSideJob -> {
            updateState { it.copy(infiniteSideJobRunning = true) }
            observeFlows(
                "InfiniteSideJob",
                flow {
                    while (true) {
                        delay(1_000)
                        emit(KitchenSinkContract.Inputs.IncrementInfiniteCounter(1))
                    }
                }
            )
        }

        is KitchenSinkContract.Inputs.CancelInfiniteSideJob -> {
            updateState { it.copy(infiniteSideJobRunning = false) }
            cancelSideJob("InfiniteSideJob")
        }

        is KitchenSinkContract.Inputs.IncrementInfiniteCounter -> {
            updateState { it.copy(infiniteCounter = it.infiniteCounter + input.delta) }
        }

        is KitchenSinkContract.Inputs.ErrorRunningInput -> {
            error("error running input")
        }

        is KitchenSinkContract.Inputs.ErrorRunningEvent -> {
            postEvent(KitchenSinkContract.Events.ErrorRunningEvent())
        }

        is KitchenSinkContract.Inputs.ErrorRunningSideJob -> {
            sideJob("ErrorRunningSideJob") {
                error("error running sideJob")
            }
        }

        is KitchenSinkContract.Inputs.ShutDownGracefully -> {
            killSwitch.requestGracefulShutdown()
        }
    }
}
