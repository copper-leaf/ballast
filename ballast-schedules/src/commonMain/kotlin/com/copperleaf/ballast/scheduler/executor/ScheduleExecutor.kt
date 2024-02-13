package com.copperleaf.ballast.scheduler.executor

public interface ScheduleExecutor {

    public enum class DelayMode {
        FireAndForget,
        Suspend,
    }
}
