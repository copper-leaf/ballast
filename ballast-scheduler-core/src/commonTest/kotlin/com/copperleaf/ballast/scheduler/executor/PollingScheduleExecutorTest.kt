package com.copperleaf.ballast.scheduler.executor

import com.copperleaf.ballast.scheduler.ScheduleExecutor
import com.copperleaf.ballast.scheduler.TestClock
import com.copperleaf.ballast.scheduler.executor.poll.InMemoryScheduleState
import com.copperleaf.ballast.scheduler.executor.poll.PollingScheduleExecutor
import com.copperleaf.ballast.scheduler.firstTen
import com.copperleaf.ballast.scheduler.firstTenWithNames
import com.copperleaf.ballast.scheduler.operators.named
import com.copperleaf.ballast.scheduler.operators.until
import com.copperleaf.ballast.scheduler.schedule.EveryHourSchedule
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
import kotlinx.datetime.toLocalDateTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

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

        val executor = PollingScheduleExecutor(
            scheduleState = InMemoryScheduleState(),
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
    }

    @Test
    fun testCatchUpBehavior_Skip() = runTest {
        advanceTimeBy(startInstant.toEpochMilliseconds())

        val executor = PollingScheduleExecutor(
            scheduleState = InMemoryScheduleState(mapOf("EveryHour" to startInstant.minus(4.hours))),
            clock = TestClock(),
            timeZone = timeZone,
            pollingSchedule = EveryMinuteSchedule(0, timeZone = timeZone)
                .until(startInstant.plus(12.hours)),
            catchUpBehavior = ScheduleExecutor.CatchUpBehavior.Skip
        )

        assertEquals(
            actual = executor
                .runSchedule(EveryHourSchedule(0).named("EveryHour"))
                .firstTen(),
            expected = listOf(
                LocalDate(2023, Month.DECEMBER, 28).atTime(3, 0, 0),
                LocalDate(2023, Month.DECEMBER, 28).atTime(4, 0, 0),
                LocalDate(2023, Month.DECEMBER, 28).atTime(5, 0, 0),
                LocalDate(2023, Month.DECEMBER, 28).atTime(6, 0, 0),
                LocalDate(2023, Month.DECEMBER, 28).atTime(7, 0, 0),
                LocalDate(2023, Month.DECEMBER, 28).atTime(8, 0, 0),
                LocalDate(2023, Month.DECEMBER, 28).atTime(9, 0, 0),
                LocalDate(2023, Month.DECEMBER, 28).atTime(10, 0, 0),
                LocalDate(2023, Month.DECEMBER, 28).atTime(11, 0, 0),
                LocalDate(2023, Month.DECEMBER, 28).atTime(12, 0, 0),
            ),
        )
    }

    @Test
    fun testCatchUpBehavior_ExecuteOne() = runTest {
        advanceTimeBy(startInstant.toEpochMilliseconds())

        val executor = PollingScheduleExecutor(
            scheduleState = InMemoryScheduleState(mapOf("EveryHour" to startInstant.minus(4.hours))),
            clock = TestClock(),
            timeZone = timeZone,
            pollingSchedule = EveryMinuteSchedule(0, timeZone = timeZone)
                .until(startInstant.plus(12.hours)),
            catchUpBehavior = ScheduleExecutor.CatchUpBehavior.ExecuteOne
        )

        assertEquals(
            actual = executor
                .runSchedule(EveryHourSchedule(0).named("EveryHour"))
                .firstTen(),
            expected = listOf(
                startInstant.toLocalDateTime(timeZone),
                LocalDate(2023, Month.DECEMBER, 28).atTime(3, 0, 0),
                LocalDate(2023, Month.DECEMBER, 28).atTime(4, 0, 0),
                LocalDate(2023, Month.DECEMBER, 28).atTime(5, 0, 0),
                LocalDate(2023, Month.DECEMBER, 28).atTime(6, 0, 0),
                LocalDate(2023, Month.DECEMBER, 28).atTime(7, 0, 0),
                LocalDate(2023, Month.DECEMBER, 28).atTime(8, 0, 0),
                LocalDate(2023, Month.DECEMBER, 28).atTime(9, 0, 0),
                LocalDate(2023, Month.DECEMBER, 28).atTime(10, 0, 0),
                LocalDate(2023, Month.DECEMBER, 28).atTime(11, 0, 0),
            ),
        )
    }

    @Test
    fun testCatchUpBehavior_ExecuteAll() = runTest {
        advanceTimeBy(startInstant.toEpochMilliseconds())

        val executor = PollingScheduleExecutor(
            scheduleState = InMemoryScheduleState(mapOf("EveryHour" to startInstant.minus(4.hours))),
            clock = TestClock(),
            timeZone = timeZone,
            pollingSchedule = EveryMinuteSchedule(0, timeZone = timeZone)
                .until(startInstant.plus(12.hours)),
            catchUpBehavior = ScheduleExecutor.CatchUpBehavior.ExecuteAll
        )

        assertEquals(
            actual = executor
                .runSchedule(EveryHourSchedule(0).named("EveryHour"))
                .firstTen(),
            expected = listOf(
                LocalDate(2023, Month.DECEMBER, 27).atTime(23, 0, 0),
                LocalDate(2023, Month.DECEMBER, 28).atTime(0, 0, 0),
                LocalDate(2023, Month.DECEMBER, 28).atTime(1, 0, 0),
                LocalDate(2023, Month.DECEMBER, 28).atTime(2, 0, 0),
                LocalDate(2023, Month.DECEMBER, 28).atTime(3, 0, 0),
                LocalDate(2023, Month.DECEMBER, 28).atTime(4, 0, 0),
                LocalDate(2023, Month.DECEMBER, 28).atTime(5, 0, 0),
                LocalDate(2023, Month.DECEMBER, 28).atTime(6, 0, 0),
                LocalDate(2023, Month.DECEMBER, 28).atTime(7, 0, 0),
                LocalDate(2023, Month.DECEMBER, 28).atTime(8, 0, 0),
            ),
        )
    }
}
