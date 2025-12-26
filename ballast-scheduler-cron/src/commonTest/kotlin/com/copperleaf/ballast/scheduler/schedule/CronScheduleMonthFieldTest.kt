package com.copperleaf.ballast.scheduler.schedule

import com.copperleaf.ballast.scheduler.firstTen
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlinx.datetime.toInstant
import kotlin.test.Test
import kotlin.test.assertEquals

class CronScheduleMonthFieldTest {

    val timeZone = TimeZone.UTC
    val startDay = LocalDate(2023, Month.DECEMBER, 1)
    val startInstant = startDay.atTime(2, 37, 0).toInstant(timeZone)

    @Test
    fun test3rdOfEachMonth() {
        // Cron: "0 0 * 3 *" (at midnight every midnight in March)
        val cronExpression = CronExpression(
            minute = MinuteField.exactValue(0),
            hour = HourField.exactValue(0),
            dayOfMonth = DayOfMonthField.anyValue(),
            month = MonthField.exactValue(Month.MARCH),
            dayOfWeek = DayOfWeekField.anyValue(),
            timeZone = timeZone,
        )

        assertEquals(
            actual = CronSchedule(cronExpression)
                .generateSchedule(startInstant)
                .firstTen(timeZone),
            expected = listOf(
                LocalDate(2024, Month.MARCH, 1).atTime(0, 0),
                LocalDate(2024, Month.MARCH, 2).atTime(0, 0),
                LocalDate(2024, Month.MARCH, 3).atTime(0, 0),
                LocalDate(2024, Month.MARCH, 4).atTime(0, 0),
                LocalDate(2024, Month.MARCH, 5).atTime(0, 0),
                LocalDate(2024, Month.MARCH, 6).atTime(0, 0),
                LocalDate(2024, Month.MARCH, 7).atTime(0, 0),
                LocalDate(2024, Month.MARCH, 8).atTime(0, 0),
                LocalDate(2024, Month.MARCH, 9).atTime(0, 0),
                LocalDate(2024, Month.MARCH, 10).atTime(0, 0),
            ),
        )
    }
}
