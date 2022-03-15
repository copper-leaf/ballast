package com.copperleaf.ballast.debugger.ui.sample

import com.copperleaf.ballast.InputHandler
import com.copperleaf.ballast.InputHandlerScope
import com.copperleaf.ballast.observeFlows
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow

class SampleInputHandler : InputHandler<
    SampleContract.Inputs,
    SampleContract.Events,
    SampleContract.State> {
    override suspend fun InputHandlerScope<
        SampleContract.Inputs,
        SampleContract.Events,
        SampleContract.State>.handleInput(
        input: SampleContract.Inputs
    ) = when (input) {
        is SampleContract.Inputs.CloseSampleWindow -> {
            postEvent(SampleContract.Events.CloseWindow)
        }

        is SampleContract.Inputs.LongRunningInput -> {
            updateState { it.copy(loading = true) }
            delay(5000)
            updateState { it.copy(loading = false) }
        }

        is SampleContract.Inputs.LongRunningEvent -> {
            postEvent(SampleContract.Events.LongRunningEvent())
        }
        is SampleContract.Inputs.LongRunningSideEffect -> {
            sideEffect("LongRunningSideEffect") {
                delay(5000)
            }
        }
        is SampleContract.Inputs.InfiniteSideEffect -> {
            observeFlows(
                "InfiniteSideEffect",
                flow {
                    while (true) {
                        delay(1_000)
                        emit(SampleContract.Inputs.IncrementInfiniteCounter(1))
                    }
                }
            )
        }
        is SampleContract.Inputs.CancelInfiniteSideEffect -> {
            sideEffect("InfiniteSideEffect") {
                // run a side-effect with the same key, so the infinite flow one gets cancelled, while this one runs
                // to completion
            }
        }
        is SampleContract.Inputs.IncrementInfiniteCounter -> {
            updateState { it.copy(infiniteCounter = it.infiniteCounter + input.delta) }
        }

        is SampleContract.Inputs.ErrorRunningInput -> {
            error("error running input")
        }
        is SampleContract.Inputs.ErrorRunningEvent -> {
            postEvent(SampleContract.Events.ErrorRunningEvent())
        }
        is SampleContract.Inputs.ErrorRunningSideEffect -> {
            sideEffect("ErrorRunningSideEffect") {
                error("error running sideEffect")
            }
        }
    }
}
