package com.copperleaf.ballast.examples.scheduler.memory.schedule

import com.copperleaf.ballast.examples.scheduler.memory.InMemorySchedulesContract
import com.copperleaf.ballast.scheduler.SchedulerAdapter
import com.copperleaf.ballast.scheduler.SchedulerAdapterScope
import com.copperleaf.ballast.scheduler.operators.delayed
import com.copperleaf.ballast.scheduler.operators.named
import com.copperleaf.ballast.scheduler.schedule.EveryDaySchedule
import com.copperleaf.ballast.scheduler.schedule.EveryHourSchedule
import com.copperleaf.ballast.scheduler.schedule.EveryMinuteSchedule
import com.copperleaf.ballast.scheduler.schedule.FixedDelaySchedule
import kotlinx.datetime.LocalTime
import kotlin.time.Duration.Companion.seconds

public class InMemorySchedulesAdapter : SchedulerAdapter<
        InMemorySchedulesContract.Inputs,
        InMemorySchedulesContract.Events,
        InMemorySchedulesContract.State> {
    companion object {
        val fixed = "Every Second"
        val everyMinute = "Twice Every Minute"
        val everyHour = "6 Times Every Hour"
        val everyDay = "4 Times Every day"
    }

    override suspend fun SchedulerAdapterScope<
            InMemorySchedulesContract.Inputs,
            InMemorySchedulesContract.Events,
            InMemorySchedulesContract.State>.configureSchedules() {
        onSchedule(
            schedule = FixedDelaySchedule(1.seconds)
                .delayed(1.5.seconds)
                .named(fixed),
            scheduledInput = { InMemorySchedulesContract.Inputs.Increment(fixed, 1) }
        )

        onSchedule(
            schedule = EveryMinuteSchedule(3, 33)
                .delayed(1.5.seconds)
                .named(everyMinute),
            scheduledInput = { InMemorySchedulesContract.Inputs.Increment(everyMinute, 10) }
        )

        onSchedule(
            schedule = EveryHourSchedule(4, 14, 24, 34, 44, 54)
                .delayed(1.5.seconds)
                .named(everyHour),
            scheduledInput = { InMemorySchedulesContract.Inputs.Increment(everyHour, 10_000) }
        )

        onSchedule(
            schedule = EveryDaySchedule(LocalTime(6, 0), LocalTime(12, 0), LocalTime(18, 0), LocalTime(0, 0))
                .delayed(1.5.seconds)
                .named(everyDay),
            scheduledInput = { InMemorySchedulesContract.Inputs.Increment(everyDay, 100_000) }
        )
    }
}
