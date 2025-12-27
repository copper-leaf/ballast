package com.copperleaf.ballast.scheduler

public interface SchedulerAdapterScope<Inputs : Any, Events : Any, State : Any> {

    public fun <T : Inputs> onSchedule(
        schedule: NamedSchedule,
        scheduledInput: () -> T,
    )
}
