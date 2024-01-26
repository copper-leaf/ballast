package com.copperleaf.ballast.examples.scheduler

import com.copperleaf.ballast.scheduler.SchedulerAdapter
import com.copperleaf.ballast.scheduler.SchedulerAdapterScope
import com.copperleaf.ballast.scheduler.schedule.EveryDaySchedule
import com.copperleaf.ballast.scheduler.schedule.EveryHourSchedule
import com.copperleaf.ballast.scheduler.schedule.FixedDelaySchedule
import kotlinx.datetime.LocalTime
import kotlin.time.Duration.Companion.minutes

public class AndroidSchedulerExampleAdapter :
    SchedulerAdapter<
            SchedulerExampleContract.Inputs,
            SchedulerExampleContract.Events,
            SchedulerExampleContract.State> {
    companion object {
        private val twiceAnHour = "At 0 and 30 minutes"
        private val twiceDaily = "At 9:47 AM and PM"
        private val every63Minutes = "Every 63 minutes"
    }

    override suspend fun SchedulerAdapterScope<
            SchedulerExampleContract.Inputs,
            SchedulerExampleContract.Events,
            SchedulerExampleContract.State>.configureSchedules() {
        onSchedule(
            key = twiceAnHour,
            schedule = EveryHourSchedule(0, 30),
            scheduledInput = { SchedulerExampleContract.Inputs.Increment(twiceAnHour, 1) }
        )
        onSchedule(
            key = twiceDaily,
            schedule = EveryDaySchedule(LocalTime(9, 47), LocalTime(21, 47)),
            scheduledInput = { SchedulerExampleContract.Inputs.Increment(twiceDaily, 1) }
        )
        onSchedule(
            key = every63Minutes,
            schedule = FixedDelaySchedule(63.minutes),
            scheduledInput = { SchedulerExampleContract.Inputs.Increment(every63Minutes, 1) }
        )
    }
}
