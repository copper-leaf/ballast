package com.copperleaf.ballast.scheduler.executor

import com.copperleaf.ballast.scheduler.NamedSchedule
import com.copperleaf.ballast.scheduler.Schedule
import com.copperleaf.ballast.scheduler.ScheduleEmission
import com.copperleaf.ballast.scheduler.ScheduleExecutor
import com.copperleaf.ballast.scheduler.operators.getNext
import com.copperleaf.ballast.scheduler.schedule.EveryMinuteSchedule
import com.copperleaf.ballast.scheduler.utils.generateSafeSchedule
import com.copperleaf.ballast.scheduler.utils.isBeforeMinute
import com.copperleaf.ballast.scheduler.utils.isSameOrBeforeMinute
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.flow
import kotlinx.datetime.TimeZone
import kotlin.time.Clock
import kotlin.time.Instant

public class PollingScheduleExecutor(
    private val scheduleState: ScheduleExecutor.State,
    private val clock: Clock = Clock.System,
    private val timeZone: TimeZone = TimeZone.UTC,
    private val pollingSchedule: Schedule = EveryMinuteSchedule(0, timeZone = timeZone),
    private val catchUpBehavior: ScheduleExecutor.CatchUpBehavior = ScheduleExecutor.CatchUpBehavior.ExecuteOne,
) : ScheduleExecutor {

    override fun runSchedule(schedule: NamedSchedule): Flow<ScheduleEmission> = flow {
        val pollingStartTime = clock.now()

        // emit any missed executions since we last ran this schedule, if needed
        catchUpExecutions(pollingStartTime, schedule)

        // start polling for future executions every minute, and emit when the schedule matches
        startPollingSchedule(pollingStartTime) { nextScheduleInstant ->
            handleScheduledTaskIfReady(
                pollingStartTime,
                nextScheduleInstant,
                schedule,
            )
        }
    }

    override fun runSchedules(schedules: List<NamedSchedule>): Flow<ScheduleEmission> = flow {
        val pollingStartTime = clock.now()

        // emit any missed executions since we last ran this schedule, if needed. Each schedule is caught up individually
        schedules.forEach { schedule ->
            catchUpExecutions(pollingStartTime, schedule)
        }

        // start polling for future executions every minute, and emit when the schedule matches. Each schedule is
        // checked individually, but all values will be emitted downstream through the same Flow
        startPollingSchedule(pollingStartTime) { nextScheduleInstant ->
            schedules.forEach { schedule ->
                handleScheduledTaskIfReady(
                    pollingStartTime,
                    nextScheduleInstant,
                    schedule,
                )
            }
        }
    }

    private suspend inline fun startPollingSchedule(
        pollingStartTime: Instant,
        onClockTick: (Instant) -> Unit,
    ) {
        pollingSchedule
            .generateSafeSchedule(pollingStartTime)
            .forEach { nextScheduleInstant ->
                // wait the appropriate amount of time until we hit the next scheduled instant
                val currentInstant = clock.now()
                val delayDuration = nextScheduleInstant - currentInstant
                delay(delayDuration)
                onClockTick(nextScheduleInstant)
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
        if (nextScheduleInstant.isSameOrBeforeMinute(currentInstant, timeZone)) {
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

    private suspend fun FlowCollector<ScheduleEmission>.catchUpExecutions(
        pollingStartTime: Instant,
        schedule: NamedSchedule,
    ) {
        val scheduleStartTime = (scheduleState.getLastExecution(schedule) ?: pollingStartTime)
        // get the next scheduled time for this schedule based on the last execution time, and coerce it to the next
        // future minute
        val nextScheduleInstant = schedule.getNext(scheduleStartTime) ?: return

        if (nextScheduleInstant.isBeforeMinute(pollingStartTime, timeZone)) {
            // we have missed at least one scheduled execution
            when (catchUpBehavior) {
                ScheduleExecutor.CatchUpBehavior.Skip -> {
                    // do nothing, but store the latest execution time so the schedule does not try to catch up once
                    // we start polling.
                    scheduleState.storeExecution(schedule, pollingStartTime)
                }

                ScheduleExecutor.CatchUpBehavior.ExecuteOne -> {
                    // emit one missed execution
                    emit(
                        ScheduleEmission(
                            triggeredAt = pollingStartTime,
                            name = schedule.name,
                            schedule = schedule,
                        )
                    )
                    scheduleState.storeExecution(schedule, pollingStartTime)
                }

                ScheduleExecutor.CatchUpBehavior.ExecuteAll -> {
                    // emit all missed executions
                    var missedScheduleInstant = nextScheduleInstant
                    while (missedScheduleInstant.isBeforeMinute(pollingStartTime, timeZone)) {
                        emit(
                            ScheduleEmission(
                                triggeredAt = missedScheduleInstant,
                                name = schedule.name,
                                schedule = schedule,
                            )
                        )
                        scheduleState.storeExecution(schedule, missedScheduleInstant)
                        missedScheduleInstant = schedule.getNext(missedScheduleInstant) ?: break
                    }
                }
            }
        }
    }
}
