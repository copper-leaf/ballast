package com.copperleaf.ballast.queue.vm

import com.copperleaf.ballast.InputHandler
import com.copperleaf.ballast.InputHandlerScope
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.seconds

class TestInputHandler : InputHandler<
        TestContract.Inputs,
        TestContract.Events,
        TestContract.State> {
    override suspend fun InputHandlerScope<
            TestContract.Inputs,
            TestContract.Events,
            TestContract.State>.handleInput(
        input: TestContract.Inputs
    ): Unit = when (input) {
        is TestContract.Inputs.AsyncJob -> {
            updateState { it.copy(step = 1) }
            delay(3.seconds)
            updateState { it.copy(step = 2) }
            delay(3.seconds)
            updateState { it.copy(step = 3) }
            delay(3.seconds)
            postEvent(TestContract.Events.JobCompleted(input.inputData.uppercase()))
        }
    }
}
