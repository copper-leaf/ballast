package com.copperleaf.ballast.examples.scheduler

import com.copperleaf.ballast.scheduler.workmanager.SchedulerCallback

public class AndroidSchedulerExampleCallback : SchedulerCallback<SchedulerExampleContract.Inputs> {

    override suspend fun dispatchInput(input: SchedulerExampleContract.Inputs) {
        check(input is SchedulerExampleContract.Inputs.Increment)

        Notifications.notify(
            context = MainApp.INSTANCE!!,
            title = "Ballast Scheduler",
            message = input.scheduleKey
        )
    }
}
