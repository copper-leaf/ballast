package com.copperleaf.ballast.scheduler.schedule

import com.copperleaf.ballast.scheduler.firstTen
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlinx.datetime.toInstant
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals

@Ignore
class CronScheduleDayOfWeekFieldTest {

    val timeZone = TimeZone.UTC
    val startDay = LocalDate(2023, Month.DECEMBER, 1)
    val startInstant = startDay.atTime(2, 37, 0).toInstant(timeZone)

    @Test
    fun testEveryTuesday() {
        // Cron: "0 0 * * 2" (at midnight every Tuesday)
        val cronExpression = CronExpression(
            minute = MinuteField(0),
            hour = HourField(0),
            dayOfMonth = DayOfMonthField((1..31).toList()),
            month = MonthField(Month.entries.toList()),
            dayOfWeek = DayOfWeekField(DayOfWeek.TUESDAY),
            timeZone = timeZone,
        )

        assertEquals(
            actual = CronSchedule(cronExpression)
                .generateSchedule(startInstant)
                .firstTen(timeZone),
            expected = listOf(
                LocalDate(2023, Month.DECEMBER, 5).atTime(0, 0),
                LocalDate(2023, Month.DECEMBER, 12).atTime(0, 0),
                LocalDate(2023, Month.DECEMBER, 19).atTime(0, 0),
                LocalDate(2023, Month.DECEMBER, 26).atTime(0, 0),
                LocalDate(2024, Month.JANUARY, 2).atTime(0, 0),
                LocalDate(2024, Month.JANUARY, 9).atTime(0, 0),
                LocalDate(2024, Month.JANUARY, 16).atTime(0, 0),
                LocalDate(2024, Month.JANUARY, 23).atTime(0, 0),
                LocalDate(2024, Month.JANUARY, 30).atTime(0, 0),
                LocalDate(2024, Month.FEBRUARY, 6).atTime(0, 0),
            ),
        )
    }
}
