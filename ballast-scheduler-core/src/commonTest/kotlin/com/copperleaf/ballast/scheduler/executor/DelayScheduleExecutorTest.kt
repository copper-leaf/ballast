package com.copperleaf.ballast.scheduler.executor

import com.copperleaf.ballast.scheduler.TestClock
import com.copperleaf.ballast.scheduler.firstTen
import com.copperleaf.ballast.scheduler.operators.named
import com.copperleaf.ballast.scheduler.operators.until
import com.copperleaf.ballast.scheduler.schedule.EveryMinuteSchedule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.onEach
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
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
public class DelayScheduleExecutorTest {

    val timeZone = TimeZone.UTC
    val startDay = LocalDate(2023, Month.DECEMBER, 28)
    val startInstant = startDay.atTime(2, 37, 0).toInstant(timeZone)
    val schedule = EveryMinuteSchedule(12)
        .until(startInstant.plus(10.minutes))
        .named("EveryMinuteAt12Seconds")

    @Test
    fun fastCollector() = runTest {
        advanceTimeBy(startInstant.toEpochMilliseconds())

        val missedTasks = mutableListOf<Instant>()
        val executor = DelayScheduleExecutor(TestClock(), onTaskDropped = { missedTasks += it })

        assertEquals(
            actual = executor
                .runSchedule(schedule)
                .firstTen(),
            expected = listOf(
                startDay.atTime(2, 37, 12),
                startDay.atTime(2, 38, 12),
                startDay.atTime(2, 39, 12),
                startDay.atTime(2, 40, 12),
                startDay.atTime(2, 41, 12),
                startDay.atTime(2, 42, 12),
                startDay.atTime(2, 43, 12),
                startDay.atTime(2, 44, 12),
                startDay.atTime(2, 45, 12),
                startDay.atTime(2, 46, 12),
            ),
        )
        assertEquals(
            actual = missedTasks,
            expected = emptyList(),
        )
    }

    @Test
    fun slowCollector() = runTest {
        advanceTimeBy(startInstant.toEpochMilliseconds())

        val missedTasks = mutableListOf<Instant>()
        val executor = DelayScheduleExecutor(TestClock(), onTaskDropped = { missedTasks += it })

        assertEquals(
            actual = executor
                .runSchedule(schedule)
                .onEach { delay(5.minutes) }
                .firstTen(),
            expected = listOf(
                startDay.atTime(2, 37, 12),
                startDay.atTime(2, 42, 12),
            ),
        )
        assertEquals(
            actual = missedTasks
                .map { it.toLocalDateTime(timeZone) },
            expected = listOf(
                startDay.atTime(2, 38, 12),
                startDay.atTime(2, 39, 12),
                startDay.atTime(2, 40, 12),
                startDay.atTime(2, 41, 12),
                startDay.atTime(2, 43, 12),
                startDay.atTime(2, 44, 12),
                startDay.atTime(2, 45, 12),
                startDay.atTime(2, 46, 12),
            ),
        )
    }
}
