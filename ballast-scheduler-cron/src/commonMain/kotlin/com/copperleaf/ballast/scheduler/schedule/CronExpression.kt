package com.copperleaf.ballast.scheduler.schedule

import com.copperleaf.ballast.scheduler.utils.adjust
import com.copperleaf.ballast.scheduler.utils.plusMinutes
import com.copperleaf.ballast.scheduler.utils.update
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.atTime
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.number
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Instant

@Suppress("SimpleRedundantLet")
public data class CronExpression(
    val minute: MinuteField,
    val hour: HourField,
    val dayOfMonth: DayOfMonthField,
    val month: MonthField,
    val dayOfWeek: DayOfWeekField,
    internal val timeZone: TimeZone = TimeZone.UTC,
) {
    public fun nextMatchingInstant(current: Instant): Instant {
        var currentTime = current.adjust(timeZone) {
            update(second = 0, nanosecond = 0)
        }

        while (true) {
            val updatedTime = advanceToNextMatchingTime(currentTime)
            if (matches(updatedTime)) {
                return updatedTime
            }

            currentTime = updatedTime.plusMinutes(1, timeZone)
        }
    }

    internal fun advanceToNextMatchingTime(after: Instant): Instant {
        var time = after
        time = advanceToNextMatchingMonth(time)
        time = advanceToNextMatchingDay(time)
        time = advanceToNextMatchingHour(time)
        time = advanceToNextMatchingMinute(time)

        return time
    }

    internal fun matches(time: Instant): Boolean {
        val tDateTime = time.toLocalDateTime(timeZone)
        return minute.matches(tDateTime.minute) &&
                hour.matches(tDateTime.hour) &&
                dayOfMonth.matches(tDateTime.day) &&
                month.matches(tDateTime.month.number) &&
                dayOfWeek.matches(tDateTime.dayOfWeek.isoDayNumber % 7)
    }

    internal fun advanceToNextMatchingMonth(time: Instant): Instant {
        val tDateTime = time.toLocalDateTime(timeZone)
        val next = month.nextOrSame(tDateTime.month.number)

        return if (next != null) {
            tDateTime
                .update(month = Month.entries[next - 1])
                .toInstant(timeZone)
        } else {
            LocalDate(
                year = tDateTime.year + 1,
                month = 1,
                day = 1,
            )
                .atStartOfDayIn(timeZone)
        }
    }

    internal fun advanceToNextMatchingDay(time: Instant): Instant {
        var tInstant = time

        while (true) {
            val tDateTime = tInstant.toLocalDateTime(timeZone)
            val domMatch = dayOfMonth.matches(tDateTime.day)
            val dowMatch = dayOfWeek.matches(tDateTime.dayOfWeek.isoDayNumber % 7)

//            // TODO
//            val dayMatches = if (dayOfMonth.isWildcard || dayOfWeek.isWildcard) {
//                domMatch && dowMatch
//            } else {
//                domMatch || dowMatch
//            }
            val dayMatches = domMatch || dowMatch

            if (dayMatches) return tInstant

            tInstant = tInstant
                .plus(1.days)
                .toLocalDateTime(timeZone)
                .date
                .atStartOfDayIn(timeZone)
        }
    }

    internal fun advanceToNextMatchingHour(time: Instant): Instant {
        val tDateTime = time.toLocalDateTime(timeZone)

        val next = hour.nextOrSame(tDateTime.hour)

        return if (next != null) {
            tDateTime
                .let {
                    it.date.atTime(
                        hour = next,
                        minute = tDateTime.minute,
                        second = tDateTime.second,
                        nanosecond = tDateTime.nanosecond,
                    )
                }
                .toInstant(timeZone)
        } else {
            time
                .plus(1.days)
                .toLocalDateTime(timeZone)
                .let {
                    it.date.atTime(
                        hour = 0,
                        minute = 0,
                        second = 0,
                        nanosecond = 0,
                    )
                }
                .toInstant(timeZone)
        }
    }

    internal fun advanceToNextMatchingMinute(time: Instant): Instant {
        val tDateTime = time.toLocalDateTime(timeZone)

        val next = minute.nextOrSame(tDateTime.minute)

        return if (next != null) {
            tDateTime
                .let {
                    it.date.atTime(
                        hour = tDateTime.hour,
                        minute = next,
                        second = tDateTime.second,
                        nanosecond = tDateTime.nanosecond,
                    )
                }
                .toInstant(timeZone)
        } else {
            time
                .plus(1.hours)
                .toLocalDateTime(timeZone)
                .let {
                    it.date.atTime(
                        hour = it.hour,
                        minute = 0,
                        second = 0,
                        nanosecond = 0,
                    )
                }
                .toInstant(timeZone)
        }
    }
}
