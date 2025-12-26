package com.copperleaf.ballast.scheduler.schedule

import com.copperleaf.ballast.scheduler.firstTen
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlinx.datetime.toInstant
import kotlin.test.Test
import kotlin.test.assertEquals

class CronScheduleMinuteFieldTest {

    val timeZone = TimeZone.UTC
    val startDay = LocalDate(2023, Month.DECEMBER, 1)
    val startInstant = startDay.atTime(2, 37, 0).toInstant(timeZone)

    @Test
    fun testEvery30Minutes() {
        // Cron: "*/30 * * * *" (at the top and bottom of every hour)
        val cronExpression = CronExpression(
            minute = MinuteField.anyValue(step = 30),
            hour = HourField.anyValue(),
            dayOfMonth = DayOfMonthField.anyValue(),
            month = MonthField.anyValue(),
            dayOfWeek = DayOfWeekField.anyValue(),
            timeZone = timeZone,
        )

        assertEquals(
            actual = CronSchedule(cronExpression)
                .generateSchedule(startInstant)
                .firstTen(timeZone),
            expected = listOf(
                LocalDate(2023, Month.DECEMBER, 1).atTime(3, 0),
                LocalDate(2023, Month.DECEMBER, 1).atTime(3, 30),
                LocalDate(2023, Month.DECEMBER, 1).atTime(4, 0),
                LocalDate(2023, Month.DECEMBER, 1).atTime(4, 30),
                LocalDate(2023, Month.DECEMBER, 1).atTime(5, 0),
                LocalDate(2023, Month.DECEMBER, 1).atTime(5, 30),
                LocalDate(2023, Month.DECEMBER, 1).atTime(6, 0),
                LocalDate(2023, Month.DECEMBER, 1).atTime(6, 30),
                LocalDate(2023, Month.DECEMBER, 1).atTime(7, 0),
                LocalDate(2023, Month.DECEMBER, 1).atTime(7, 30),
            ),
        )
    }
}
