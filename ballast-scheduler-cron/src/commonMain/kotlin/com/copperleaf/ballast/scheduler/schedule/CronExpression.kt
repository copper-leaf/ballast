package com.copperleaf.ballast.scheduler.schedule

import com.copperleaf.ballast.scheduler.parser.CronExpressionParser
import com.copperleaf.ballast.scheduler.utils.adjust
import com.copperleaf.ballast.scheduler.utils.number
import com.copperleaf.ballast.scheduler.utils.update
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.atTime
import kotlinx.datetime.number
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant

@Suppress("SimpleRedundantLet")
public data class CronExpression(
    val minute: MinuteField = MinuteField.anyValue(),
    val hour: HourField = HourField.anyValue(),
    val dayOfMonth: DayOfMonthField = DayOfMonthField.anyValue(),
    val month: MonthField = MonthField.anyValue(),
    val dayOfWeek: DayOfWeekField = DayOfWeekField.anyValue(),
    internal val timeZone: TimeZone = TimeZone.UTC,
) {
    public fun nextMatchingInstant(current: Instant): Instant {
        // start at the top of the next minute, to ensure values are always matching in the future
        // relative to `current` if it is also a valid match
        var currentTime = current
            .plus(1.minutes)
            .adjust(timeZone) {
                update(second = 0, nanosecond = 0)
            }

        while (true) {
            val updatedTime = advanceToNextMatchingTime(currentTime)
            if (matches(updatedTime)) {
                return updatedTime
            }

            currentTime = updatedTime.plus(1.minutes)
        }
    }

    internal fun advanceToNextMatchingTime(after: Instant): Instant {
        val time0 = after
        val time1 = advanceToNextMatchingMonth(time0)
        val time2 = advanceToNextMatchingDay(time1)
        val time3 = advanceToNextMatchingHour(time2)
        val time4 = advanceToNextMatchingMinute(time3)

        return time4
    }

    internal fun matches(time: Instant): Boolean {
        val tDateTime = time.toLocalDateTime(timeZone)
        return minute.matches(tDateTime.minute) &&
                hour.matches(tDateTime.hour) &&
                dayOfMonth.matches(tDateTime.day) &&
                month.matches(tDateTime.month.number) &&
                dayOfWeek.matches(tDateTime.dayOfWeek.number)
    }

    internal fun advanceToNextMatchingMonth(time: Instant): Instant {
        val tDateTime = time.toLocalDateTime(timeZone)
        val next = month.nextOrSame(tDateTime.month.number)

        return if (next == tDateTime.month.number) {
            // the current month matches. Don't adjust the month
            return time
        } else if (next != null) {
            // the current month is not valid, but another exists later in the year. Adjust to the start of that month
            tDateTime
                .update(month = Month.entries[next - 1], day = 1)
                .date.atStartOfDayIn(timeZone)
        } else {
            // no more valid months this year, advance to the first valid month next year
            LocalDate(
                year = tDateTime.year + 1,
                month = Month.JANUARY,
                day = 1,
            ).atStartOfDayIn(timeZone)
        }
    }

    internal fun advanceToNextMatchingDay(time: Instant): Instant {
        var tInstant = time

        while (true) {
            val tDateTime = tInstant.toLocalDateTime(timeZone)
            val domMatch = dayOfMonth.matches(tDateTime.day)
            val dowMatch = dayOfWeek.matches(tDateTime.dayOfWeek.number)

            // According to standard CRON semantics, when either day-of-month or day-of-week is a wildcard (*), the
            // other field is used exclusively. If neither are wildcards, a match occurs when either field matches
            val dayMatches = if (dayOfMonth.wildcard) {
                dowMatch
            } else if (dayOfWeek.wildcard) {
                domMatch
            } else {
                domMatch || dowMatch
            }

            if (dayMatches) {
                return tInstant
            } else {
                tInstant = tInstant
                    .plus(1.days)
                    .toLocalDateTime(timeZone)
                    .date
                    .atStartOfDayIn(timeZone)
            }
        }
    }

    internal fun advanceToNextMatchingHour(time: Instant): Instant {
        val tDateTime = time.toLocalDateTime(timeZone)

        val next = hour.nextOrSame(tDateTime.hour)

        return if (next == tDateTime.hour) {
            // the current hour matches. Don't adjust the hour
            time
        } else if (next != null) {
            // the current hour is not valid, but another exists later in the day. Adjust to that hour
            tDateTime
                .let {
                    it.date.atTime(
                        hour = next,
                        minute = 0,
                    )
                }
                .toInstant(timeZone)
        } else {
            time
                .plus(1.days)
                .toLocalDateTime(timeZone)
                .date
                .atStartOfDayIn(timeZone)
        }
    }

    internal fun advanceToNextMatchingMinute(time: Instant): Instant {
        val tDateTime = time.toLocalDateTime(timeZone)

        val next = minute.nextOrSame(tDateTime.minute)

        return if (next == tDateTime.minute) {
            // the current minute matches. Don't adjust the minute
            time
        } else if (next != null) {
            // the current minute is not valid, but another exists later in the hour. Adjust to that minute
            tDateTime
                .let {
                    it.date.atTime(
                        hour = tDateTime.hour,
                        minute = next,
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
                    )
                }
                .toInstant(timeZone)
        }
    }

    public companion object {
        public fun parse(expression: String, timeZone: TimeZone = TimeZone.UTC): CronExpression {
            return CronExpressionParser.parse(expression, timeZone)
        }
    }
}
