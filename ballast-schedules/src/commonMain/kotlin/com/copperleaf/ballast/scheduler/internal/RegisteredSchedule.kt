package com.copperleaf.ballast.scheduler.internal

import com.copperleaf.ballast.scheduler.executor.ScheduleExecutor
import com.copperleaf.ballast.scheduler.schedule.Schedule

internal class RegisteredSchedule<I : Any, E : Any, S : Any>(
    val key: String,
    val schedule: Schedule,
    val delayMode: ScheduleExecutor.DelayMode,
    val scheduledInput: () -> I,
)
