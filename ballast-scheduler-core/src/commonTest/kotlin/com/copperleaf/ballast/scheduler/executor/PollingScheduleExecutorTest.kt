package com.copperleaf.ballast.scheduler.executor

import com.copperleaf.ballast.scheduler.NamedSchedule
import com.copperleaf.ballast.scheduler.ScheduleExecutor
import com.copperleaf.ballast.scheduler.TestClock
import com.copperleaf.ballast.scheduler.firstTenWithNames
import com.copperleaf.ballast.scheduler.operators.named
import com.copperleaf.ballast.scheduler.operators.until
import com.copperleaf.ballast.scheduler.schedule.EveryMinuteSchedule
import com.copperleaf.ballast.scheduler.schedule.FixedDelaySchedule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlinx.datetime.toInstant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
public class PollingScheduleExecutorTest {

    val timeZone = TimeZone.UTC
    val startDay = LocalDate(2023, Month.DECEMBER, 28)
    val startInstant = startDay.atTime(2, 37, 0).toInstant(timeZone)

    val schedule1 = EveryMinuteSchedule(12)
        .until(startInstant.plus(10.minutes))
        .named("EveryMinuteAt12Seconds")
    val schedule2 = FixedDelaySchedule(3.minutes)
        .until(startInstant.plus(10.minutes))
        .named("Every3Minutes")

    val pollingSchedule = EveryMinuteSchedule(0, timeZone = timeZone)
        .until(startInstant.plus(10.minutes))

    @Test
    fun fastCollector() = runTest {
        advanceTimeBy(startInstant.toEpochMilliseconds())

        val missedTasks = mutableListOf<Instant>()
        val executor = PollingScheduleExecutor(
            scheduleState = TestScheduleState(),
            clock = TestClock(),
            timeZone = timeZone,
            pollingSchedule = pollingSchedule,
        )

        assertEquals(
            actual = executor
                .runSchedules(listOf(schedule1, schedule2))
                .firstTenWithNames(),
            expected = listOf(
                "EveryMinuteAt12Seconds" to startDay.atTime(2, 38, 0),
                "EveryMinuteAt12Seconds" to startDay.atTime(2, 39, 0),
                "EveryMinuteAt12Seconds" to startDay.atTime(2, 40, 0),
                "Every3Minutes" to startDay.atTime(2, 40, 0),
                "EveryMinuteAt12Seconds" to startDay.atTime(2, 41, 0),
                "EveryMinuteAt12Seconds" to startDay.atTime(2, 42, 0),
                "EveryMinuteAt12Seconds" to startDay.atTime(2, 43, 0),
                "Every3Minutes" to startDay.atTime(2, 43, 0),
                "EveryMinuteAt12Seconds" to startDay.atTime(2, 44, 0),
                "EveryMinuteAt12Seconds" to startDay.atTime(2, 45, 0),
            ),
        )
        assertEquals(
            actual = missedTasks,
            expected = emptyList(),
        )
    }

    class TestScheduleState : ScheduleExecutor.State {
        private val lastExecutions: MutableMap<String, Instant> = mutableMapOf()

        override suspend fun getLastExecution(schedule: NamedSchedule): Instant? {
            return lastExecutions[schedule.name]
        }

        override suspend fun storeExecution(
            schedule: NamedSchedule,
            instant: Instant
        ) {
            lastExecutions[schedule.name] = instant
        }
    }
}
