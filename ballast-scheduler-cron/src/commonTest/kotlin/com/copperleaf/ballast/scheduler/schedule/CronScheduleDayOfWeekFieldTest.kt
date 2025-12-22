package com.copperleaf.ballast.scheduler.schedule

import com.copperleaf.ballast.scheduler.firstTen
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlinx.datetime.toInstant
import kotlin.test.Test
import kotlin.test.assertEquals

class CronScheduleDayOfWeekFieldTest {

    val timeZone = TimeZone.UTC
    val startDay = LocalDate(2023, Month.DECEMBER, 1)
    val startInstant = startDay.atTime(2, 37, 0).toInstant(timeZone)

    @Test
    fun testEveryTuesday() {
        // Cron: "0 0 * * 2" (at midnight every Tuesday)
        val cronExpression = CronExpression(
            minute = MinuteField(ExactValue(0, 59, 0)),
            hour = HourField(ExactValue(0, 23, 0)),
            dayOfMonth = DayOfMonthField(AnyValue(1, 31)),
            month = MonthField(AnyValue(1, 12)),
            dayOfWeek = DayOfWeekField(ExactValue(0, 6, 2)),
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
