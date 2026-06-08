package com.copperleaf.ballast.examples.scheduler.layout

import com.copperleaf.ballast.InputHandler
import com.copperleaf.ballast.InputHandlerScope

class SchedulerExampleLayoutInputHandler : InputHandler<
        SchedulerExampleLayoutContract.Inputs,
        SchedulerExampleLayoutContract.Events,
        SchedulerExampleLayoutContract.State> {
    override suspend fun InputHandlerScope<
            SchedulerExampleLayoutContract.Inputs,
            SchedulerExampleLayoutContract.Events,
            SchedulerExampleLayoutContract.State>.handleInput(
        input: SchedulerExampleLayoutContract.Inputs
    ): Unit = when (input) {
        is SchedulerExampleLayoutContract.Inputs.ChangeTab -> {
            updateState { it.copy(tab = input.tab) }
        }
    }
}
