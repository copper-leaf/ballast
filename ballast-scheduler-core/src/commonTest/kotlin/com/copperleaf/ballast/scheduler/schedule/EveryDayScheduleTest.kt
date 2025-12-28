package com.copperleaf.ballast.scheduler.schedule

import com.copperleaf.ballast.scheduler.firstTen
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
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

    @Test
    fun handlesDaylightSavingsSpringForward() = runTest {
        // DST starts on 2024-03-10 in America/New_York (2:00 AM jumps to 3:00 AM)
        val tz = TimeZone.of("America/New_York")
        val startDay = LocalDate(2024, Month.MARCH, 9)
        val startInstant = startDay.atTime(1, 0).toInstant(tz)

        assertEquals(
            actual = EveryDaySchedule(LocalTime(2, 30), timeZone = tz)
                .generateSchedule(startInstant)
                .take(3)
                .map { it.toLocalDateTime(tz) }
                .toList(),
            expected = listOf(
                LocalDate(2024, Month.MARCH, 9).atTime(2, 30),
                // 2024-03-10 2:30 does not exist, so should be scheduled at 3:30
                LocalDate(2024, Month.MARCH, 10).atTime(3, 30),
                LocalDate(2024, Month.MARCH, 11).atTime(2, 30),
            )
        )
    }

    @Test
    fun handlesDaylightSavingsFallBack() = runTest {
        // DST ends on 2024-11-03 in America/New_York (2:00 AM repeats)
        val tz = TimeZone.of("America/New_York")
        val startDay = LocalDate(2024, Month.NOVEMBER, 2)
        val startInstant = startDay.atTime(1, 0).toInstant(tz)

        assertEquals(
            actual = EveryDaySchedule(LocalTime(1, 30), timeZone = tz)
                .generateSchedule(startInstant)
                .take(3)
                .map { it.toLocalDateTime(tz) }
                .toList(),
            expected = listOf(
                LocalDate(2024, Month.NOVEMBER, 2).atTime(1, 30),
                LocalDate(2024, Month.NOVEMBER, 3).atTime(1, 30), // occurs twice, but should only schedule once
                LocalDate(2024, Month.NOVEMBER, 4).atTime(1, 30),
            )
        )
    }

    @Test
    fun handlesLeapDayInLeapYear() = runTest {
        val tz = TimeZone.UTC
        val startDay = LocalDate(2024, Month.FEBRUARY, 27) // 2024 is a leap year
        val startInstant = startDay.atTime(10, 0).toInstant(tz)

        assertEquals(
            actual = EveryDaySchedule(LocalTime(12, 0), timeZone = tz)
                .generateSchedule(startInstant)
                .take(4)
                .map { it.toLocalDateTime(tz) }
                .toList(),
            expected = listOf(
                LocalDate(2024, Month.FEBRUARY, 27).atTime(12, 0),
                LocalDate(2024, Month.FEBRUARY, 28).atTime(12, 0),
                LocalDate(2024, Month.FEBRUARY, 29).atTime(12, 0), // Leap Day
                LocalDate(2024, Month.MARCH, 1).atTime(12, 0),
            )
        )
    }

    @Test
    fun skipsLeapDayInNonLeapYear() = runTest {
        val tz = TimeZone.UTC
        val startDay = LocalDate(2023, Month.FEBRUARY, 27) // 2023 is not a leap year
        val startInstant = startDay.atTime(10, 0).toInstant(tz)

        assertEquals(
            actual = EveryDaySchedule(LocalTime(12, 0), timeZone = tz)
                .generateSchedule(startInstant)
                .take(3)
                .map { it.toLocalDateTime(tz) }
                .toList(),
            expected = listOf(
                LocalDate(2023, Month.FEBRUARY, 27).atTime(12, 0),
                LocalDate(2023, Month.FEBRUARY, 28).atTime(12, 0),
                LocalDate(2023, Month.MARCH, 1).atTime(12, 0),
            )
        )
    }
}
