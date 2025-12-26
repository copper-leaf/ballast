package com.copperleaf.ballast.scheduler.schedule

import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlinx.datetime.toInstant
import kotlin.test.Ignore
import kotlin.test.Test

class CronScheduleMonthFieldTest {

    val timeZone = TimeZone.UTC
    val startDay = LocalDate(2023, Month.DECEMBER, 28)
    val startInstant = startDay.atTime(2, 37, 0).toInstant(timeZone)

    @Test
    @Ignore
    fun testEveryDayInMarch() {
//        // Cron: "0 0 * 3 *" (every midnight in March)
//        val cronExpression = CronExpression(
//            minute = MinuteField(ExactValue(0, 59, 0)),
//            hour = HourField(ExactValue(0, 23, 0)),
//            dayOfMonth = DayOfMonthField(AnyValue(1, 31)),
//            month = MonthField(ExactValue(1, 12, 3)),
//            dayOfWeek = DayOfWeekField(AnyValue(0, 6)),
//            timeZone = timeZone,
//        )
//
//        assertEquals(
//            actual = CronSchedule(cronExpression)
//                .generateSchedule(startInstant)
//                .firstTen(timeZone),
//            expected = listOf(
//                LocalDate(2024, Month.MARCH, 1).atTime(0, 0),
//                LocalDate(2024, Month.MARCH, 2).atTime(0, 0),
//                LocalDate(2024, Month.MARCH, 3).atTime(0, 0),
//                LocalDate(2024, Month.MARCH, 4).atTime(0, 0),
//                LocalDate(2024, Month.MARCH, 5).atTime(0, 0),
//                LocalDate(2024, Month.MARCH, 6).atTime(0, 0),
//                LocalDate(2024, Month.MARCH, 7).atTime(0, 0),
//                LocalDate(2024, Month.MARCH, 8).atTime(0, 0),
//                LocalDate(2024, Month.MARCH, 9).atTime(0, 0),
//                LocalDate(2024, Month.MARCH, 10).atTime(0, 0),
//            ),
//        )
    }
}
