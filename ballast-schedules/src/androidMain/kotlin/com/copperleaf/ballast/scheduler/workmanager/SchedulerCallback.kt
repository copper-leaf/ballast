package com.copperleaf.ballast.scheduler.workmanager

import androidx.work.OneTimeWorkRequest

public interface SchedulerCallback<Inputs> {
    public suspend fun dispatchInput(input: Inputs)

    /**
     * Override this function to add any custom configurations to each [OneTimeWorkRequest.Builder] that Ballast
     * configures for each schedule. This can be used to mark the request as expedited, apply constraints, etc.
     */
    public fun configureWorkRequest(
        builder: OneTimeWorkRequest.Builder,
    ): OneTimeWorkRequest.Builder {
        return builder
    }
}
