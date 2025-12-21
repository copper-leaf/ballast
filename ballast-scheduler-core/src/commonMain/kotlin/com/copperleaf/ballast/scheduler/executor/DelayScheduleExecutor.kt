package com.copperleaf.ballast.scheduler.executor

import com.copperleaf.ballast.scheduler.NamedSchedule
import com.copperleaf.ballast.scheduler.ScheduleEmission
import com.copperleaf.ballast.scheduler.ScheduleExecutor
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
        schedule: NamedSchedule,
    ): Flow<ScheduleEmission> = flow {
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
                        ScheduleEmission(
                            triggeredAt = nextScheduleInstant,
                            name = schedule.name,
                            schedule = schedule,
                        )
                    )
                } else {
                    // report the scheduled task as having been dropped, so it can be logged or otherwise handled
                    onTaskDropped(nextScheduleInstant)
                }
            }
    }

    override fun runSchedules(schedules: List<NamedSchedule>): Flow<ScheduleEmission> {
        return schedules
            .map { runSchedule(it) }
            .merge()
    }
}
