package com.copperleaf.ballast.scheduler.schedule

import com.copperleaf.ballast.scheduler.firstTen
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlinx.datetime.toInstant
import kotlin.test.Test
import kotlin.test.assertEquals

class EveryDayScheduleTest {
    val timeZone = TimeZone.UTC
    val startDay = LocalDate(2023, Month.DECEMBER, 28)
    val startInstant = startDay.atTime(1, 0).toInstant(timeZone)

    @Test
    fun onceEveryDayTest() = runTest {
        assertEquals(
            actual = EveryDaySchedule(LocalTime(2, 37))
                .generateSchedule(startInstant)
                .firstTen(),
            expected = listOf(
                LocalDate(2023, Month.DECEMBER, 28).atTime(2, 37),
                LocalDate(2023, Month.DECEMBER, 29).atTime(2, 37),
                LocalDate(2023, Month.DECEMBER, 30).atTime(2, 37),
                LocalDate(2023, Month.DECEMBER, 31).atTime(2, 37),
                LocalDate(2024, Month.JANUARY, 1).atTime(2, 37),
                LocalDate(2024, Month.JANUARY, 2).atTime(2, 37),
                LocalDate(2024, Month.JANUARY, 3).atTime(2, 37),
                LocalDate(2024, Month.JANUARY, 4).atTime(2, 37),
                LocalDate(2024, Month.JANUARY, 5).atTime(2, 37),
                LocalDate(2024, Month.JANUARY, 6).atTime(2, 37),
            ),
        )
    }

    @Test
    fun multipleTimesEveryDayTest() = runTest {
        assertEquals(
            actual = EveryDaySchedule(LocalTime(2, 37), LocalTime(7, 38), LocalTime(23, 58))
                .generateSchedule(startInstant)
                .firstTen(),
            expected = listOf(
                LocalDate(2023, Month.DECEMBER, 28).atTime(2, 37),
                LocalDate(2023, Month.DECEMBER, 28).atTime(7, 38),
                LocalDate(2023, Month.DECEMBER, 28).atTime(23, 58),
                LocalDate(2023, Month.DECEMBER, 29).atTime(2, 37),
                LocalDate(2023, Month.DECEMBER, 29).atTime(7, 38),
                LocalDate(2023, Month.DECEMBER, 29).atTime(23, 58),
                LocalDate(2023, Month.DECEMBER, 30).atTime(2, 37),
                LocalDate(2023, Month.DECEMBER, 30).atTime(7, 38),
                LocalDate(2023, Month.DECEMBER, 30).atTime(23, 58),
                LocalDate(2023, Month.DECEMBER, 31).atTime(2, 37),
            ),
        )
    }
}
