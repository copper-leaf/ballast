package com.copperleaf.ballast.scheduler.schedule

import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlinx.datetime.toInstant
import kotlin.test.Test

class CronScheduleMinuteFieldTest {

    val timeZone = TimeZone.UTC
    val startDay = LocalDate(2023, Month.DECEMBER, 28)
    val startInstant = startDay.atTime(2, 37, 0).toInstant(timeZone)

    @Test
    fun testEvery30Minutes() {
//        // Cron: "*/30 * * * *" (every 30 minutes)
//        val cronExpression = CronExpression(
//            minute = MinuteField(AnyValue(0, 59, 30)),
//            hour = HourField(AnyValue(0, 23)),
//            dayOfMonth = DayOfMonthField(AnyValue(1, 31)),
//            month = MonthField(AnyValue(1, 12)),
//            dayOfWeek = DayOfWeekField(AnyValue(0, 6)),
//            timeZone = timeZone,
//        )
//
//        assertEquals(
//            actual = CronSchedule(cronExpression)
//                .generateSchedule(startInstant)
//                .firstTen(timeZone),
//            expected = listOf(
//                startDay.atTime(3, 0),
//                startDay.atTime(3, 30),
//                startDay.atTime(4, 0),
//                startDay.atTime(4, 30),
//                startDay.atTime(5, 0),
//                startDay.atTime(5, 30),
//                startDay.atTime(6, 0),
//                startDay.atTime(6, 30),
//                startDay.atTime(7, 0),
//                startDay.atTime(7, 30),
//            ),
//        )
    }
}
