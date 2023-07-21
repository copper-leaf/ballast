package com.copperleaf.ballast.contracts.dispatcher

import com.copperleaf.ballast.InputHandler
import com.copperleaf.ballast.InputHandlerScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlin.coroutines.coroutineContext

@OptIn(ExperimentalStdlibApi::class)
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
            val inputDispatcher = coroutineContext[CoroutineDispatcher]
            updateState {
                it.copy(actualInputDispatcher = inputDispatcher)
            }
            postEvent(DispatcherTestContract.Events.GetEventDispatcher)
            sideJob("get sideJob dispatcher") {
                postInput(
                    DispatcherTestContract.Inputs.SetSideJobDispatcher(
                        actualSideJobDispatcher = coroutineContext[CoroutineDispatcher]
                    )
                )
            }
        }

        is DispatcherTestContract.Inputs.SetEventDispatcher -> {
            updateState {
                it.copy(actualEventDispatcher = input.actualEventDispatcher)
            }
        }

        is DispatcherTestContract.Inputs.SetInterceptorDispatcher -> {
            updateState {
                it.copy(actualInterceptorDispatcher = input.actualInterceptorDispatcher)
            }
        }

        is DispatcherTestContract.Inputs.SetSideJobDispatcher -> {
            updateState {
                it.copy(actualSideJobDispatcher = input.actualSideJobDispatcher)
            }
        }
    }
}
