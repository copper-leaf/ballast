package com.copperleaf.ballast.scheduler.schedule

import com.copperleaf.ballast.scheduler.utils.plusDays
import com.copperleaf.ballast.scheduler.utils.plusHours
import com.copperleaf.ballast.scheduler.utils.plusMinutes
import com.copperleaf.ballast.scheduler.utils.plusSeconds
import com.copperleaf.ballast.scheduler.utils.plusYears
import com.copperleaf.ballast.scheduler.utils.withDayOfMonth
import com.copperleaf.ballast.scheduler.utils.withHour
import com.copperleaf.ballast.scheduler.utils.withMinute
import com.copperleaf.ballast.scheduler.utils.withMonth
import com.copperleaf.ballast.scheduler.utils.withNano
import com.copperleaf.ballast.scheduler.utils.withSecond
import kotlinx.datetime.TimeZone
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant

public data class CronExpression(
    val minute: MinuteField,
    val hour: HourField,
    val dayOfMonth: DayOfMonthField,
    val month: MonthField,
    val dayOfWeek: DayOfWeekField,
    private val timeZone: TimeZone = TimeZone.UTC,
) {
    public fun nextMatchingInstant(after: Instant): Instant {
        var time = after
            .plusSeconds(60, timeZone)
            .withSecond(0, timeZone)
            .withNano(0, timeZone)

        while (true) {
            time = adjustMonth(time)
            time = adjustDay(time)
            time = adjustHour(time)
            time = adjustMinute(time)

            if (matches(time)) {
                return time
            }

            time = time.plusMinutes(1, timeZone)
        }
    }

    private fun matches(time: Instant): Boolean {
        val tDateTime = time.toLocalDateTime(timeZone)
        return minute.matches(tDateTime.minute) &&
                hour.matches(tDateTime.hour) &&
                dayOfMonth.matches(tDateTime.day) &&
                month.matches(tDateTime.month.number) &&
                dayOfWeek.matches(tDateTime.dayOfWeek.isoDayNumber % 7)
    }

    private fun adjustMinute(time: Instant): Instant {
        val tDateTime = time.toLocalDateTime(timeZone)

        val next = minute.nextOrSame(tDateTime.minute)
            ?: return time
                .plusHours(1, timeZone)
                .withMinute(0, timeZone)

        return time.withMinute(next, timeZone)
    }

    private fun adjustHour(time: Instant): Instant {
        val tDateTime = time.toLocalDateTime(timeZone)

        val next = hour.nextOrSame(tDateTime.hour)
            ?: return time
                .plusDays(1, timeZone)
                .withHour(0, timeZone)
                .withMinute(0, timeZone)

        return time.withHour(next, timeZone)
    }

    private fun adjustMonth(time: Instant): Instant {
        val tDateTime = time.toLocalDateTime(timeZone)

        val next = month.nextOrSame(tDateTime.month.number)
            ?: return time
                .plusYears(1, timeZone)
                .withMonth(1, timeZone)
                .withDayOfMonth(1, timeZone)
                .withHour(0, timeZone)
                .withMinute(0, timeZone)

        return time.withMonth(next, timeZone)
    }

    private fun adjustDay(time: Instant): Instant {
        var tInstant = time

        while (true) {
            val tDateTime = tInstant.toLocalDateTime(timeZone)
            val domMatch = dayOfMonth.matches(tDateTime.day)
            val dowMatch = dayOfWeek.matches(tDateTime.dayOfWeek.isoDayNumber % 7)

            val dayMatches = if (dayOfMonth.isWildcard || dayOfWeek.isWildcard) {
                domMatch && dowMatch
            } else {
                domMatch || dowMatch
            }

            if (dayMatches) return tInstant

            tInstant = tInstant
                .plusDays(1, timeZone)
                .withHour(0, timeZone)
                .withMinute(0, timeZone)
        }
    }
}
