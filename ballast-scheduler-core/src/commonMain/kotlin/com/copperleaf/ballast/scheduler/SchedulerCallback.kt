package com.copperleaf.ballast.scheduler

public interface SchedulerCallback {
    public suspend fun handleTask()
}
