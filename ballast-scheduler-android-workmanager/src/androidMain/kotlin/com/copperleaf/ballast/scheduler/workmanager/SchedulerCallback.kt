package com.copperleaf.ballast.scheduler.workmanager

public interface SchedulerCallback {
    public suspend fun handleTask()
}
