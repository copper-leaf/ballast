package com.copperleaf.ballast.scheduler.executor

import com.copperleaf.ballast.scheduler.NamedSchedule
import com.copperleaf.ballast.scheduler.Schedule
import com.copperleaf.ballast.scheduler.ScheduleEmission
import com.copperleaf.ballast.scheduler.ScheduleExecutor
import com.copperleaf.ballast.scheduler.operators.getNext
import com.copperleaf.ballast.scheduler.schedule.EveryMinuteSchedule
import com.copperleaf.ballast.scheduler.utils.generateSafeSchedule
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.flow
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.Instant

// TODO: handle catch-up behavior for schedules that were missed while the executor was not running
public class PollingScheduleExecutor(
    private val scheduleState: ScheduleExecutor.State,
    private val clock: Clock = Clock.System,
    private val timeZone: TimeZone = TimeZone.UTC,
    private val pollingSchedule: Schedule = EveryMinuteSchedule(0, timeZone = timeZone),
) : ScheduleExecutor {

    override fun runSchedule(schedule: NamedSchedule): Flow<ScheduleEmission> = flow {
        val pollingStartTime = clock.now()

        pollingSchedule
            .generateSafeSchedule(clock.now())
            .forEach { nextScheduleInstant ->
                handleScheduledTaskIfReady(
                    pollingStartTime,
                    nextScheduleInstant,
                    schedule,
                )
            }
    }

    override fun runSchedules(schedules: List<NamedSchedule>): Flow<ScheduleEmission> = flow {
        val pollingStartTime = clock.now()

        pollingSchedule
            .generateSafeSchedule(clock.now())
            .forEach { nextScheduleInstant ->
                schedules.forEach { schedule ->
                    handleScheduledTaskIfReady(
                        pollingStartTime,
                        nextScheduleInstant,
                        schedule,
                    )
                }
            }
    }

    private suspend fun FlowCollector<ScheduleEmission>.handleScheduledTaskIfReady(
        pollingStartTime: Instant,
        currentInstant: Instant,
        schedule: NamedSchedule,
    ) {
        // get the last execution time for this schedule. If the schedule has never been executed, consider the first
        // moment this polling executor started running as the last execution time, so delay-based schedules do not drift
        // but always calculate their next execution time from a stable moment in time. The next scheduled time will be
        // calculated from this point.
        val scheduleStartTime = (scheduleState.getLastExecution(schedule) ?: pollingStartTime)

        // get the next scheduled time for this schedule based on the last execution time, and coerce it to the next
        // future minute
        val nextScheduleInstant = schedule.getNext(scheduleStartTime) ?: return

        // if the next scheduled time matches the current time, store the execution time and emit it
        if (nextScheduleInstant.isSameOrBeforeMinute(currentInstant)) {
            emit(
                ScheduleEmission(
                    triggeredAt = currentInstant,
                    name = schedule.name,
                    schedule = schedule,
                )
            )
            scheduleState.storeExecution(schedule, currentInstant)
        }
    }

    private fun Instant.isSameOrBeforeMinute(other: Instant): Boolean {
        val a = this.toLocalDateTime(timeZone)
        val b = other.toLocalDateTime(timeZone)

        if (a.year < b.year) return true
        if (a.month < b.month) return true
        if (a.day < b.day) return true
        if (a.hour < b.hour) return true
        if (a.minute < b.minute) return true
        if (a.minute == b.minute) return true

        return false
    }
}
