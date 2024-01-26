package com.copperleaf.ballast.scheduler.workmanager

public interface SchedulerCallback<Inputs> {
    public suspend fun dispatchInput(input: Inputs)
}
