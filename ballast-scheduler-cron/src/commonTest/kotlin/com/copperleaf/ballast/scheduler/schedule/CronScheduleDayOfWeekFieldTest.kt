package com.copperleaf.ballast.scheduler.schedule

import com.copperleaf.ballast.scheduler.firstTen
import kotlinx.datetime.DayOfWeek
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
    fun testEveryWednesday() {
        // Cron: "0 0 * * 3" (at midnight every Wednesday)
        val cronExpression = CronExpression(
            minute = MinuteField.exactValue(0),
            hour = HourField.exactValue(0),
            dayOfMonth = DayOfMonthField.anyValue(),
            month = MonthField.anyValue(),
            dayOfWeek = DayOfWeekField.exactValue(DayOfWeek.WEDNESDAY),
            timeZone = timeZone,
        )

        assertEquals(
            actual = CronSchedule(cronExpression)
                .generateSchedule(startInstant)
                .firstTen(timeZone),
            expected = listOf(
                LocalDate(2023, Month.DECEMBER, 6).atTime(0, 0),
                LocalDate(2023, Month.DECEMBER, 13).atTime(0, 0),
                LocalDate(2023, Month.DECEMBER, 20).atTime(0, 0),
                LocalDate(2023, Month.DECEMBER, 27).atTime(0, 0),
                LocalDate(2024, Month.JANUARY, 3).atTime(0, 0),
                LocalDate(2024, Month.JANUARY, 10).atTime(0, 0),
                LocalDate(2024, Month.JANUARY, 17).atTime(0, 0),
                LocalDate(2024, Month.JANUARY, 24).atTime(0, 0),
                LocalDate(2024, Month.JANUARY, 31).atTime(0, 0),
                LocalDate(2024, Month.FEBRUARY, 7).atTime(0, 0),
            ),
        )
    }
}
