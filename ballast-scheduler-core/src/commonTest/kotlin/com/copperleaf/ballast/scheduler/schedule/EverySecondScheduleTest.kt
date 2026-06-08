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

class EverySecondScheduleTest {
    val timeZone = TimeZone.UTC
    val startDay = LocalDate(2023, Month.DECEMBER, 28)
    val startInstant = startDay.atTime(2, 37, 52).toInstant(timeZone)

    @Test
    fun everySecondTest() = runTest {
        assertEquals(
            actual = EverySecondSchedule()
                .generateSchedule(startInstant)
                .firstTen(),
            expected = listOf(
                startDay.atTime(2, 37, 53),
                startDay.atTime(2, 37, 54),
                startDay.atTime(2, 37, 55),
                startDay.atTime(2, 37, 56),
                startDay.atTime(2, 37, 57),
                startDay.atTime(2, 37, 58),
                startDay.atTime(2, 37, 59),
                startDay.atTime(2, 38, 0),
                startDay.atTime(2, 38, 1),
                startDay.atTime(2, 38, 2),
            ),
        )
    }
}
