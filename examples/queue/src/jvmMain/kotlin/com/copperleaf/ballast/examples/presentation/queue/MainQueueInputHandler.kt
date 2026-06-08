package com.copperleaf.ballast.examples.presentation.queue

import com.copperleaf.ballast.InputHandler
import com.copperleaf.ballast.InputHandlerScope
import kotlinx.coroutines.delay

class MainQueueInputHandler : InputHandler<
        MainQueueContract.Inputs,
        MainQueueContract.Events,
        MainQueueContract.State> {
    override suspend fun InputHandlerScope<
            MainQueueContract.Inputs,
            MainQueueContract.Events,
            MainQueueContract.State>.handleInput(
        input: MainQueueContract.Inputs
    ): Unit = when (input) {
        is MainQueueContract.Inputs.MainJob -> {
            val state = updateStateAndGet { it.copy(step = it.step + 1) }

            // simulate a long-running job working
            delay(input.processingTime)

            // simulate possible failures
            if (state.step < input.successAttemptIndex) {
                throw IllegalStateException("Simulated job failure on attempt ${state.step}")
            }

            // assuming the job succeeded and did not timeout, emit the completion event if necessary
            if (input.resultValue != null) {
                postEvent(
                    MainQueueContract.Events.JobCompleted(
                        resultValue = input.resultValue,
                    )
                )
            }

            Unit
        }
    }
}
