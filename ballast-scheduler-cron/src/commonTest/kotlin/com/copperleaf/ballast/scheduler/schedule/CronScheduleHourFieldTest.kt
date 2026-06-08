package com.copperleaf.ballast.scheduler.schedule

import com.copperleaf.ballast.scheduler.firstTen
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlinx.datetime.toInstant
import kotlin.test.Test
import kotlin.test.assertEquals

class CronScheduleHourFieldTest {

    val timeZone = TimeZone.UTC
    val startDay = LocalDate(2023, Month.DECEMBER, 1)
    val startInstant = startDay.atTime(2, 37, 0).toInstant(timeZone)

    @Test
    fun testEvery4Hours() {
        // Cron: "0 */4 * * *" (every 4 hours at the top of the hour)
        val cronExpression = CronExpression(
            minute = MinuteField.exactValue(0),
            hour = HourField.anyValue(step = 4),
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
                LocalDate(2023, Month.DECEMBER, 1).atTime(4, 0),
                LocalDate(2023, Month.DECEMBER, 1).atTime(8, 0),
                LocalDate(2023, Month.DECEMBER, 1).atTime(12, 0),
                LocalDate(2023, Month.DECEMBER, 1).atTime(16, 0),
                LocalDate(2023, Month.DECEMBER, 1).atTime(20, 0),
                LocalDate(2023, Month.DECEMBER, 2).atTime(0, 0),
                LocalDate(2023, Month.DECEMBER, 2).atTime(4, 0),
                LocalDate(2023, Month.DECEMBER, 2).atTime(8, 0),
                LocalDate(2023, Month.DECEMBER, 2).atTime(12, 0),
                LocalDate(2023, Month.DECEMBER, 2).atTime(16, 0),
            ),
        )
    }
}
