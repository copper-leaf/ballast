package com.copperleaf.ballast.scheduler.operators

import com.copperleaf.ballast.scheduler.firstTen
import com.copperleaf.ballast.scheduler.schedule.EveryDaySchedule
import com.copperleaf.ballast.scheduler.schedule.EveryMinuteSchedule
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlinx.datetime.toInstant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.DurationUnit

class AlignOperatorsTest {
    val timeZone = TimeZone.UTC
    val startDay = LocalDate(2023, Month.DECEMBER, 28)
    val startInstant = startDay.atTime(2, 37, 0).toInstant(timeZone)

    @Test
    fun scheduleAlignToSecondsTest() = runTest {
        // Instants already on second boundaries are returned unchanged
        assertEquals(
            actual = EveryMinuteSchedule(30)
                .alignTo(DurationUnit.SECONDS, timeZone)
                .generateSchedule(startInstant)
                .firstTen(),
            expected = listOf(
                startDay.atTime(2, 37, 30),
                startDay.atTime(2, 38, 30),
                startDay.atTime(2, 39, 30),
                startDay.atTime(2, 40, 30),
                startDay.atTime(2, 41, 30),
                startDay.atTime(2, 42, 30),
                startDay.atTime(2, 43, 30),
                startDay.atTime(2, 44, 30),
                startDay.atTime(2, 45, 30),
                startDay.atTime(2, 46, 30),
            ),
        )
    }

    @Test
    fun scheduleAlignToMinutesTest() = runTest {
        // Instants at :30 seconds are bumped forward to the top of the next minute
        assertEquals(
            actual = EveryMinuteSchedule(30)
                .alignTo(DurationUnit.MINUTES, timeZone)
                .generateSchedule(startInstant)
                .firstTen(),
            expected = listOf(
                startDay.atTime(2, 38, 0),
                startDay.atTime(2, 39, 0),
                startDay.atTime(2, 40, 0),
                startDay.atTime(2, 41, 0),
                startDay.atTime(2, 42, 0),
                startDay.atTime(2, 43, 0),
                startDay.atTime(2, 44, 0),
                startDay.atTime(2, 45, 0),
                startDay.atTime(2, 46, 0),
                startDay.atTime(2, 47, 0),
            ),
        )
    }

    @Test
    fun scheduleAlignToHoursTest() = runTest {
        // Instants at :30 past the hour are bumped forward to the top of the next hour
        assertEquals(
            actual = EveryDaySchedule(LocalTime(9, 30))
                .alignTo(DurationUnit.HOURS, timeZone)
                .generateSchedule(startInstant)
                .firstTen(),
            expected = listOf(
                LocalDate(2023, Month.DECEMBER, 28).atTime(10, 0),
                LocalDate(2023, Month.DECEMBER, 29).atTime(10, 0),
                LocalDate(2023, Month.DECEMBER, 30).atTime(10, 0),
                LocalDate(2023, Month.DECEMBER, 31).atTime(10, 0),
                LocalDate(2024, Month.JANUARY, 1).atTime(10, 0),
                LocalDate(2024, Month.JANUARY, 2).atTime(10, 0),
                LocalDate(2024, Month.JANUARY, 3).atTime(10, 0),
                LocalDate(2024, Month.JANUARY, 4).atTime(10, 0),
                LocalDate(2024, Month.JANUARY, 5).atTime(10, 0),
                LocalDate(2024, Month.JANUARY, 6).atTime(10, 0),
            ),
        )
    }

    @Test
    fun scheduleAlignToDaysTest() = runTest {
        // Instants at 09:00 are bumped forward to midnight of the next day
        assertEquals(
            actual = EveryDaySchedule(LocalTime(9, 0))
                .alignTo(DurationUnit.DAYS, timeZone)
                .generateSchedule(startInstant)
                .firstTen(),
            expected = listOf(
                LocalDate(2023, Month.DECEMBER, 29).atTime(0, 0),
                LocalDate(2023, Month.DECEMBER, 30).atTime(0, 0),
                LocalDate(2023, Month.DECEMBER, 31).atTime(0, 0),
                LocalDate(2024, Month.JANUARY, 1).atTime(0, 0),
                LocalDate(2024, Month.JANUARY, 2).atTime(0, 0),
                LocalDate(2024, Month.JANUARY, 3).atTime(0, 0),
                LocalDate(2024, Month.JANUARY, 4).atTime(0, 0),
                LocalDate(2024, Month.JANUARY, 5).atTime(0, 0),
                LocalDate(2024, Month.JANUARY, 6).atTime(0, 0),
                LocalDate(2024, Month.JANUARY, 7).atTime(0, 0),
            ),
        )
    }
}
