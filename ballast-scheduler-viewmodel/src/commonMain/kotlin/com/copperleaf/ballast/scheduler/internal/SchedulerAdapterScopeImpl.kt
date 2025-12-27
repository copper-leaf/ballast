package com.copperleaf.ballast.scheduler.internal

import com.copperleaf.ballast.scheduler.NamedSchedule
import com.copperleaf.ballast.scheduler.SchedulerAdapterScope

internal class SchedulerAdapterScopeImpl<I : Any, E : Any, S : Any> : SchedulerAdapterScope<I, E, S> {

    internal val schedules = mutableListOf<RegisteredSchedule<I, E, S>>()

    override fun <T : I> onSchedule(
        schedule: NamedSchedule,
        scheduledInput: () -> T,
    ) {
        schedules += RegisteredSchedule(
            schedule = schedule,
            scheduledInput = scheduledInput,
        )
    }
}
