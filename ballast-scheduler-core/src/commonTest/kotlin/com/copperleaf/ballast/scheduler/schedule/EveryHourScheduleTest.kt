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

class EveryHourScheduleTest {
    val timeZone = TimeZone.UTC
    val startDay = LocalDate(2023, Month.DECEMBER, 28)
    val startInstant = startDay.atTime(2, 37).toInstant(timeZone)

    @Test
    fun onceEveryHourTest() = runTest {
        assertEquals(
            actual = EveryHourSchedule(1)
                .generateSchedule(startInstant)
                .firstTen(),
            expected = listOf(
                startDay.atTime(3, 1),
                startDay.atTime(4, 1),
                startDay.atTime(5, 1),
                startDay.atTime(6, 1),
                startDay.atTime(7, 1),
                startDay.atTime(8, 1),
                startDay.atTime(9, 1),
                startDay.atTime(10, 1),
                startDay.atTime(11, 1),
                startDay.atTime(12, 1),
            ),
        )
    }

    @Test
    fun multipleTimesEveryHourTest() = runTest {
        assertEquals(
            actual = EveryHourSchedule(0, 15, 30, 45)
                .generateSchedule(startInstant)
                .firstTen(),
            expected = listOf(
                startDay.atTime(2, 45),
                startDay.atTime(3, 0),
                startDay.atTime(3, 15),
                startDay.atTime(3, 30),
                startDay.atTime(3, 45),
                startDay.atTime(4, 0),
                startDay.atTime(4, 15),
                startDay.atTime(4, 30),
                startDay.atTime(4, 45),
                startDay.atTime(5, 0),
            ),
        )
    }
}
