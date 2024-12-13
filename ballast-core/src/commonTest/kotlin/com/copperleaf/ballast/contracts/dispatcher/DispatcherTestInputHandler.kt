package com.copperleaf.ballast.contracts.dispatcher

import com.copperleaf.ballast.InputHandler
import com.copperleaf.ballast.InputHandlerScope

public class DispatcherTestInputHandler : InputHandler<
        DispatcherTestContract.Inputs,
        DispatcherTestContract.Events,
        DispatcherTestContract.State> {
    override suspend fun InputHandlerScope<
            DispatcherTestContract.Inputs,
            DispatcherTestContract.Events,
            DispatcherTestContract.State>.handleInput(
        input: DispatcherTestContract.Inputs
    ) = when (input) {
        is DispatcherTestContract.Inputs.Initialize -> {
            val inputCoroutineScopeInfo = getCoroutineScopeInfo()
            updateState {
                it.copy(actualInputCoroutineScopeInfo = inputCoroutineScopeInfo)
            }
            postEvent(DispatcherTestContract.Events.GetEventDispatcher)
            sideJob("get sideJob dispatcher") {
                postInput(
                    DispatcherTestContract.Inputs.SetSideJobDispatcher(
                        actualSideJobCoroutineScopeInfo = getCoroutineScopeInfo()
                    )
                )
            }
        }

        is DispatcherTestContract.Inputs.SetEventDispatcher -> {
            updateState {
                it.copy(actualEventCoroutineScopeInfo = input.actualEventCoroutineScopeInfo)
            }
        }

        is DispatcherTestContract.Inputs.SetInterceptorDispatcher -> {
            updateState {
                it.copy(actualInterceptorCoroutineScopeInfo = input.actualInterceptorCoroutineScopeInfo)
            }
        }

        is DispatcherTestContract.Inputs.SetSideJobDispatcher -> {
            updateState {
                it.copy(actualSideJobCoroutineScopeInfo = input.actualSideJobCoroutineScopeInfo)
            }
        }
    }
}
