package com.copperleaf.ballast.scheduler.internal

import com.copperleaf.ballast.scheduler.SchedulerAdapterScope
import com.copperleaf.ballast.scheduler.executor.ScheduleExecutor
import com.copperleaf.ballast.scheduler.schedule.Schedule

internal class SchedulerAdapterScopeImpl<I : Any, E : Any, S : Any> : SchedulerAdapterScope<I, E, S> {

    internal val schedules = mutableListOf<RegisteredSchedule<I, E, S>>()

    override fun <T : I> onSchedule(
        key: String,
        schedule: Schedule,
        delayMode: ScheduleExecutor.DelayMode,
        scheduledInput: () -> T,
    ) {
        schedules += RegisteredSchedule(
            key = key,
            schedule = schedule,
            delayMode = delayMode,
            scheduledInput = scheduledInput,
        )
    }
}
