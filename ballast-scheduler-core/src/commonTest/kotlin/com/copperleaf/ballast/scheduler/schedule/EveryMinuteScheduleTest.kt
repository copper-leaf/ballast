package com.copperleaf.ballast.scheduler.schedule

import com.copperleaf.ballast.scheduler.firstTen
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlinx.datetime.toInstant
import kotlin.test.Test
import kotlin.test.assertEquals

class EveryMinuteScheduleTest {
    val timeZone = TimeZone.UTC
    val startDay = LocalDate(2023, Month.DECEMBER, 28)
    val startInstant = startDay.atTime(2, 37, 0).toInstant(timeZone)

    @Test
    fun onceEveryMinuteTest() = runTest {
        assertEquals(
            actual = EveryMinuteSchedule(12)
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
    fun multipleTimesEveryMinuteTest() = runTest {
        assertEquals(
            actual = EveryMinuteSchedule(0, 15, 30, 45)
                .generateSchedule(startInstant)
                .firstTen(),
            expected = listOf(
                startDay.atTime(2, 37, 15),
                startDay.atTime(2, 37, 30),
                startDay.atTime(2, 37, 45),
                startDay.atTime(2, 38, 0),
                startDay.atTime(2, 38, 15),
                startDay.atTime(2, 38, 30),
                startDay.atTime(2, 38, 45),
                startDay.atTime(2, 39, 0),
                startDay.atTime(2, 39, 15),
                startDay.atTime(2, 39, 30),
            ),
        )
    }
}
