package com.copperleaf.ballast.scheduler.operators

import com.copperleaf.ballast.scheduler.firstTen
import com.copperleaf.ballast.scheduler.schedule.EveryDaySchedule
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlinx.datetime.toInstant
import kotlin.test.Test
import kotlin.test.assertEquals

class FilterOperatorsTest {
    val timeZone = TimeZone.UTC
    val startDay = LocalDate(2023, Month.DECEMBER, 28)
    val startInstant = startDay.atTime(2, 45, 0).toInstant(timeZone)

    @Test
    fun scheduleFilterByDayOfWeekTest() = runTest {
        assertEquals(
            actual = EveryDaySchedule(LocalTime(9, 0))
                .filterByDayOfWeek(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY, timeZone = timeZone)
                .generateSchedule(startInstant)
                .firstTen(),
            expected = listOf(
                LocalDate(2023, Month.DECEMBER, 29).atTime(9, 0),
                LocalDate(2024, Month.JANUARY, 1).atTime(9, 0),
                LocalDate(2024, Month.JANUARY, 3).atTime(9, 0),
                LocalDate(2024, Month.JANUARY, 5).atTime(9, 0),
                LocalDate(2024, Month.JANUARY, 8).atTime(9, 0),
                LocalDate(2024, Month.JANUARY, 10).atTime(9, 0),
                LocalDate(2024, Month.JANUARY, 12).atTime(9, 0),
                LocalDate(2024, Month.JANUARY, 15).atTime(9, 0),
                LocalDate(2024, Month.JANUARY, 17).atTime(9, 0),
                LocalDate(2024, Month.JANUARY, 19).atTime(9, 0),
            ),
        )
    }

    @Test
    fun scheduleWeekdaysTest() = runTest {
        assertEquals(
            actual = EveryDaySchedule(LocalTime(9, 0))
                .weekdays(timeZone = timeZone)
                .generateSchedule(startInstant)
                .firstTen(),
            expected = listOf(
                LocalDate(2023, Month.DECEMBER, 28).atTime(9, 0),
                LocalDate(2023, Month.DECEMBER, 29).atTime(9, 0),
                LocalDate(2024, Month.JANUARY, 1).atTime(9, 0),
                LocalDate(2024, Month.JANUARY, 2).atTime(9, 0),
                LocalDate(2024, Month.JANUARY, 3).atTime(9, 0),
                LocalDate(2024, Month.JANUARY, 4).atTime(9, 0),
                LocalDate(2024, Month.JANUARY, 5).atTime(9, 0),
                LocalDate(2024, Month.JANUARY, 8).atTime(9, 0),
                LocalDate(2024, Month.JANUARY, 9).atTime(9, 0),
                LocalDate(2024, Month.JANUARY, 10).atTime(9, 0),
            ),
        )
    }

    @Test
    fun scheduleWeekendsTest() = runTest {
        assertEquals(
            actual = EveryDaySchedule(LocalTime(9, 0))
                .weekends(timeZone = timeZone)
                .generateSchedule(startInstant)
                .firstTen(),
            expected = listOf(
                LocalDate(2023, Month.DECEMBER, 30).atTime(9, 0),
                LocalDate(2023, Month.DECEMBER, 31).atTime(9, 0),
                LocalDate(2024, Month.JANUARY, 6).atTime(9, 0),
                LocalDate(2024, Month.JANUARY, 7).atTime(9, 0),
                LocalDate(2024, Month.JANUARY, 13).atTime(9, 0),
                LocalDate(2024, Month.JANUARY, 14).atTime(9, 0),
                LocalDate(2024, Month.JANUARY, 20).atTime(9, 0),
                LocalDate(2024, Month.JANUARY, 21).atTime(9, 0),
                LocalDate(2024, Month.JANUARY, 27).atTime(9, 0),
                LocalDate(2024, Month.JANUARY, 28).atTime(9, 0),
            ),
        )
    }
}
