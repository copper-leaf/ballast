package com.copperleaf.ballast.examples.scheduler

import com.copperleaf.ballast.EventHandler
import com.copperleaf.ballast.EventHandlerScope

class SchedulerExampleEventHandler : EventHandler<
        SchedulerExampleContract.Inputs,
        SchedulerExampleContract.Events,
        SchedulerExampleContract.State> {
    override suspend fun EventHandlerScope<
            SchedulerExampleContract.Inputs,
            SchedulerExampleContract.Events,
            SchedulerExampleContract.State>.handleEvent(
        event: SchedulerExampleContract.Events
    ) {
    }
}
