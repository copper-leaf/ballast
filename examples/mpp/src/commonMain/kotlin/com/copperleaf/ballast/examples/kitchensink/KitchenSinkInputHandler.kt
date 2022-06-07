package com.copperleaf.ballast.examples.kitchensink

import com.copperleaf.ballast.InputHandler
import com.copperleaf.ballast.InputHandlerScope
import com.copperleaf.ballast.observeFlows
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow

class KitchenSinkInputHandler : InputHandler<
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
            postEvent(KitchenSinkContract.Events.NavigateBackwards)
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
            sideJob("InfiniteSideJob") {
                // run a side-job with the same key, so the infinite flow one gets cancelled, while this one runs
                // to completion
            }
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
    }
}
