package com.copperleaf.ballast.scheduler.schedule

import com.copperleaf.ballast.scheduler.firstTen
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlinx.datetime.toInstant
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals

class CronScheduleHourFieldTest {

    val timeZone = TimeZone.UTC
    val startDay = LocalDate(2023, Month.DECEMBER, 28)
    val startInstant = startDay.atTime(2, 37, 0).toInstant(timeZone)

    @Test
    @Ignore
    fun every4Hours() {
        // Cron: "0 */4 * * *" (every 4 hours)
        val cronExpression = CronExpression(
            minute = MinuteField(ExactValue(0, 59, 0)),
            hour = HourField(AnyValue(0, 23, 4)),
            dayOfMonth = DayOfMonthField(AnyValue(1, 31)),
            month = MonthField(AnyValue(1, 12)),
            dayOfWeek = DayOfWeekField(AnyValue(0, 6)),
            timeZone = timeZone,
        )

        assertEquals(
            actual = CronSchedule(cronExpression)
                .generateSchedule(startInstant)
                .firstTen(timeZone),
            expected = listOf(
                LocalDate(2023, Month.DECEMBER, 28).atTime(4, 0),
                LocalDate(2023, Month.DECEMBER, 28).atTime(8, 0),
                LocalDate(2023, Month.DECEMBER, 28).atTime(12, 0),
                LocalDate(2023, Month.DECEMBER, 28).atTime(16, 0),
                LocalDate(2023, Month.DECEMBER, 28).atTime(20, 0),
                LocalDate(2023, Month.DECEMBER, 29).atTime(0, 0),
                LocalDate(2023, Month.DECEMBER, 29).atTime(4, 0),
                LocalDate(2023, Month.DECEMBER, 29).atTime(8, 0),
                LocalDate(2023, Month.DECEMBER, 29).atTime(12, 0),
                LocalDate(2023, Month.DECEMBER, 29).atTime(16, 0),
            ),
        )
    }
}
