package com.copperleaf.ballast.scheduler.operators

import com.copperleaf.ballast.scheduler.firstTen
import com.copperleaf.ballast.scheduler.schedule.EveryMinuteSchedule
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlinx.datetime.toInstant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.minutes

class BoundedOperatorsTest {
    val timeZone = TimeZone.UTC
    val startDay = LocalDate(2023, Month.DECEMBER, 28)
    val startInstant = startDay.atTime(2, 33, 0).toInstant(timeZone)

    val rangeStart = startDay.atTime(2, 37, 0).toInstant(timeZone)
    val rangeEnd = startDay.atTime(2, 41, 0).toInstant(timeZone)

    val inRangeStartInstant = rangeStart.plus(1.minutes)
    val beforeRangeStartInstant = rangeStart.minus(1.minutes)
    val afterRangeStartInstant = rangeEnd.plus(1.minutes)

    @Test
    fun scheduleBoundedTest_startsBeforeWindow() = runTest {
        assertEquals(
            actual = EveryMinuteSchedule(12)
                .between(rangeStart..rangeEnd)
                .generateSchedule(startInstant)
                .firstTen(),
            expected = listOf(
                startDay.atTime(2, 37, 12),
                startDay.atTime(2, 38, 12),
                startDay.atTime(2, 39, 12),
                startDay.atTime(2, 40, 12),
            ),
        )
    }

    @Test
    fun scheduleBoundedTest_startsDuringWindow() = runTest {
        assertEquals(
            actual = EveryMinuteSchedule(12)
                .between(rangeStart..rangeEnd)
                .generateSchedule(inRangeStartInstant)
                .firstTen(),
            expected = listOf(
                startDay.atTime(2, 38, 12),
                startDay.atTime(2, 39, 12),
                startDay.atTime(2, 40, 12),
            ),
        )
    }

    @Test
    fun scheduleBoundedTest_startsAfterWindow() = runTest {
        assertEquals(
            actual = EveryMinuteSchedule(12)
                .between(rangeStart..rangeEnd)
                .generateSchedule(afterRangeStartInstant)
                .firstTen(),
            expected = emptyList(),
        )
    }

    @Test
    fun scheduleUntilTest_startsBefore() = runTest {
        assertEquals(
            actual = EveryMinuteSchedule(12)
                .until(rangeEnd)
                .generateSchedule(beforeRangeStartInstant)
                .firstTen(),
            expected = listOf(
                startDay.atTime(2, 36, 12),
                startDay.atTime(2, 37, 12),
                startDay.atTime(2, 38, 12),
                startDay.atTime(2, 39, 12),
                startDay.atTime(2, 40, 12),
            ),
        )
    }

    @Test
    fun scheduleUntilTest_startsAfter() = runTest {
        assertEquals(
            actual = EveryMinuteSchedule(12)
                .until(rangeEnd)
                .generateSchedule(afterRangeStartInstant)
                .firstTen(),
            expected = emptyList(),
        )
    }
}
