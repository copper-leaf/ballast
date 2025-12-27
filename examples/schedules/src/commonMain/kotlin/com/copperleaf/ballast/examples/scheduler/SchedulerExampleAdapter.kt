package com.copperleaf.ballast.examples.scheduler

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

public class SchedulerExampleAdapter : SchedulerAdapter<
        SchedulerExampleContract.Inputs,
        SchedulerExampleContract.Events,
        SchedulerExampleContract.State> {
    companion object {
        val fixed = "Every Second"
        val everyMinute = "Twice Every Minute"
        val everyHour = "6 Times Every Hour"
        val everyDay = "4 Times Every day"
    }

    override suspend fun SchedulerAdapterScope<
            SchedulerExampleContract.Inputs,
            SchedulerExampleContract.Events,
            SchedulerExampleContract.State>.configureSchedules() {
        onSchedule(
            schedule = FixedDelaySchedule(1.seconds)
                .delayed(1.5.seconds)
                .named(fixed),
            scheduledInput = { SchedulerExampleContract.Inputs.Increment(fixed, 1) }
        )

        onSchedule(
            schedule = EveryMinuteSchedule(3, 33)
                .delayed(1.5.seconds)
                .named(everyMinute),
            scheduledInput = { SchedulerExampleContract.Inputs.Increment(everyMinute, 10) }
        )

        onSchedule(
            schedule = EveryHourSchedule(4, 14, 24, 34, 44, 54)
                .delayed(1.5.seconds)
                .named(everyHour),
            scheduledInput = { SchedulerExampleContract.Inputs.Increment(everyHour, 10_000) }
        )

        onSchedule(
            schedule = EveryDaySchedule(LocalTime(6, 0), LocalTime(12, 0), LocalTime(18, 0), LocalTime(0, 0))
                .delayed(1.5.seconds)
                .named(everyDay),
            scheduledInput = { SchedulerExampleContract.Inputs.Increment(everyDay, 100_000) }
        )
    }
}
