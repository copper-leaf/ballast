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
import kotlin.time.Duration.Companion.hours

class DelayOperatorsTest {
    val timeZone = TimeZone.UTC
    val startDay = LocalDate(2023, Month.DECEMBER, 28)
    val startInstant = startDay.atTime(2, 37, 0).toInstant(timeZone)

    @Test
    fun scheduleDelayedTest() = runTest {
        assertEquals(
            actual = EveryMinuteSchedule(12)
                .delayed(1.hours)
                .take(4)
                .generateSchedule(startInstant)
                .firstTen(),
            expected = listOf(
                startDay.atTime(3, 37, 12),
                startDay.atTime(3, 38, 12),
                startDay.atTime(3, 39, 12),
                startDay.atTime(3, 40, 12),
            ),
        )
    }

    @Test
    fun scheduleDelayedUntilTest_earlierThanActualStartTime() = runTest {
        assertEquals(
            actual = EveryMinuteSchedule(12)
                .delayedUntil(startDay.atTime(1, 0, 0).toInstant(timeZone))
                .generateSchedule(startInstant)
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
    }

    @Test
    fun scheduleDelayedUntilTest_laterThanActualStartTime() = runTest {
        assertEquals(
            actual = EveryMinuteSchedule(12)
                .delayedUntil(startDay.atTime(4, 0, 0).toInstant(timeZone))
                .generateSchedule(startInstant)
                .firstTen(),
            expected = listOf(
                startDay.atTime(4, 0, 12),
                startDay.atTime(4, 1, 12),
                startDay.atTime(4, 2, 12),
                startDay.atTime(4, 3, 12),
                startDay.atTime(4, 4, 12),
                startDay.atTime(4, 5, 12),
                startDay.atTime(4, 6, 12),
                startDay.atTime(4, 7, 12),
                startDay.atTime(4, 8, 12),
                startDay.atTime(4, 9, 12),
            ),
        )
    }
}
