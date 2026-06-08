package com.copperleaf.ballast.scheduler.schedule.expression

import com.copperleaf.ballast.scheduler.schedule.CronExpression
import com.copperleaf.ballast.scheduler.schedule.DayOfMonthField
import com.copperleaf.ballast.scheduler.schedule.DayOfWeekField
import com.copperleaf.ballast.scheduler.schedule.HourField
import com.copperleaf.ballast.scheduler.schedule.MinuteField
import com.copperleaf.ballast.scheduler.schedule.MonthField
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toInstant
import kotlin.test.Test
import kotlin.test.assertEquals

class TestNextDayOfWeekCronExpression {

    val timeZone = TimeZone.UTC
    val startDay = LocalDate(2023, Month.JANUARY, 1) // starts on a Sunday
    val startInstant = startDay.atStartOfDayIn(timeZone)

    @Test
    fun testNextMatchingInstant() {
        val cronExpression = CronExpression(
            minute = MinuteField.anyValue(),
            hour = HourField.anyValue(),
            dayOfMonth = DayOfMonthField.anyValue(),
            month = MonthField.anyValue(),
            dayOfWeek = DayOfWeekField.exactValue(DayOfWeek.TUESDAY),
            timeZone = timeZone,
        )

        assertEquals(
            actual = cronExpression.nextMatchingInstant(startInstant),
            expected = LocalDateTime(
                date = LocalDate(year = 2023, month = Month.JANUARY, day = 3),
                time = LocalTime(hour = 0, minute = 0),
            ).toInstant(timeZone),
        )
    }
}
