package com.copperleaf.ballast.scheduler

import kotlin.time.Instant

public interface SchedulerAdapterScope<Inputs : Any, Events : Any, State : Any> {

    public fun <T : Inputs> onSchedule(
        schedule: NamedSchedule,
        scheduledInput: (Instant) -> T,
    )
}
