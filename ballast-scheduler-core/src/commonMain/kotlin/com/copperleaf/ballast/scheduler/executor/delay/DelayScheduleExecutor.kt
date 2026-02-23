package com.copperleaf.ballast.scheduler.executor.delay

import com.copperleaf.ballast.scheduler.NamedSchedule
import com.copperleaf.ballast.scheduler.Schedule
import com.copperleaf.ballast.scheduler.ScheduleExecutor
import com.copperleaf.ballast.scheduler.TriggeredTask
import com.copperleaf.ballast.scheduler.utils.generateSafeSchedule
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.merge
import kotlin.time.Clock
import kotlin.time.Instant

public class DelayScheduleExecutor(
    private val clock: Clock = Clock.System,
    private val onTaskDropped: (Instant) -> Unit = { },
) : ScheduleExecutor {

    override fun runSchedule(
        schedule: Schedule,
    ): Flow<TriggeredTask> {
        return runSchedule(null, schedule)
    }

    override fun runSchedule(
        schedule: NamedSchedule,
    ): Flow<TriggeredTask> {
        return runSchedule(schedule.name, schedule)
    }

    override fun runSchedules(schedules: List<NamedSchedule>): Flow<TriggeredTask> {
        return schedules
            .map { runSchedule(it.name, it) }
            .merge()
    }

    private fun runSchedule(
        scheduleName: String?,
        schedule: Schedule,
    ): Flow<TriggeredTask> = flow {
        schedule
            .generateSafeSchedule(clock.now())
            .forEach { nextScheduleInstant ->
                val currentInstant = clock.now()

                if (nextScheduleInstant >= currentInstant) {
                    // wait the appropriate amount of time until we hit the next scheduled instant
                    val currentInstant = clock.now()
                    val delayDuration = nextScheduleInstant - currentInstant
                    delay(delayDuration)

                    emit(
                        TriggeredTask(
                            triggeredAt = nextScheduleInstant,
                            name = scheduleName,
                            schedule = schedule,
                        )
                    )
                } else {
                    // report the scheduled task as having been dropped, so it can be logged or otherwise handled
                    onTaskDropped(nextScheduleInstant)
                }
            }
    }
}
