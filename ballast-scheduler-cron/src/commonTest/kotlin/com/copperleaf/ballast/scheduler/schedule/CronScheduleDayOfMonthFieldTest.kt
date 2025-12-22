package com.copperleaf.ballast.scheduler.schedule

import com.copperleaf.ballast.scheduler.firstTen
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlinx.datetime.toInstant
import kotlin.test.Test
import kotlin.test.assertEquals

class CronScheduleDayOfMonthFieldTest {

    val timeZone = TimeZone.UTC
    val startDay = LocalDate(2023, Month.DECEMBER, 1)
    val startInstant = startDay.atTime(2, 37, 0).toInstant(timeZone)

    @Test
    fun test3rdOfEachMonth() {
        // Cron: "0 0 3 * *" (at midnight every 3rd day of the month)
        val cronExpression = CronExpression(
            minute = MinuteField(ExactValue(0, 59, 0)),
            hour = HourField(ExactValue(0, 23, 0)),
            dayOfMonth = DayOfMonthField(ExactValue(1, 31, 3)),
            month = MonthField(AnyValue(1, 12)),
            dayOfWeek = DayOfWeekField(AnyValue(0, 6)),
            timeZone = timeZone,
        )

        assertEquals(
            actual = CronSchedule(cronExpression)
                .generateSchedule(startInstant)
                .firstTen(timeZone),
            expected = listOf(
                LocalDate(2023, Month.DECEMBER, 3).atTime(0, 0),
                LocalDate(2024, Month.JANUARY, 3).atTime(0, 0),
                LocalDate(2024, Month.FEBRUARY, 3).atTime(0, 0),
                LocalDate(2024, Month.MARCH, 3).atTime(0, 0),
                LocalDate(2024, Month.APRIL, 3).atTime(0, 0),
                LocalDate(2024, Month.MAY, 3).atTime(0, 0),
                LocalDate(2024, Month.JUNE, 3).atTime(0, 0),
                LocalDate(2024, Month.JULY, 3).atTime(0, 0),
                LocalDate(2024, Month.AUGUST, 3).atTime(0, 0),
                LocalDate(2024, Month.SEPTEMBER, 3).atTime(0, 0),
            ),
        )
    }
}
