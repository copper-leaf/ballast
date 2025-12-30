package com.copperleaf.ballast.scheduler.operators

import com.copperleaf.ballast.scheduler.Schedule
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.DurationUnit
import kotlin.time.Instant

public fun Schedule.alignTo(unit: DurationUnit, timeZone: TimeZone = TimeZone.UTC): Schedule {
    return transformSchedule { scheduleSequence ->
        sequence {
            // return the first item as-is
            val iterator = scheduleSequence.iterator()

            // for each item, align it to the specified time unit boundary. Always ensure the resulting time is
            // greater than or equal to the original time.
            while (iterator.hasNext()) {
                val next = iterator.next()
                val alignedDateTime = when (unit) {
                    DurationUnit.SECONDS -> next.alignToSecond(timeZone)
                    DurationUnit.MINUTES -> next.alignToMinute(timeZone)
                    DurationUnit.HOURS -> next.alignToHour(timeZone)
                    DurationUnit.DAYS -> next.alignToDay(timeZone)
                    else -> {
                        error("Unsupported alignment unit: $unit")
                    }
                }

                yield(alignedDateTime)
            }
        }
    }
}

private fun Instant.alignToSecond(timeZone: TimeZone): Instant {
    val alignedDateTime = this.toLocalDateTime(timeZone)
    val aligned = LocalDateTime(
        year = alignedDateTime.year,
        month = alignedDateTime.month,
        day = alignedDateTime.day,
        hour = alignedDateTime.hour,
        minute = alignedDateTime.minute,
        second = alignedDateTime.second,
        nanosecond = 0,
    )
    val alignedInstant = aligned.toInstant(timeZone)
    return if (alignedInstant >= this) {
        alignedInstant
    } else {
        alignedInstant.plus(1.seconds)
    }
}

private fun Instant.alignToMinute(timeZone: TimeZone): Instant {
    val alignedDateTime = this.toLocalDateTime(timeZone)
    val aligned = LocalDateTime(
        year = alignedDateTime.year,
        month = alignedDateTime.month,
        day = alignedDateTime.day,
        hour = alignedDateTime.hour,
        minute = alignedDateTime.minute,
        second = 0,
        nanosecond = 0,
    )
    val alignedInstant = aligned.toInstant(timeZone)
    return if (alignedInstant >= this) {
        alignedInstant
    } else {
        alignedInstant.plus(1.minutes)
    }
}

private fun Instant.alignToHour(timeZone: TimeZone): Instant {
    val alignedDateTime = this.toLocalDateTime(timeZone)
    val aligned = LocalDateTime(
        year = alignedDateTime.year,
        month = alignedDateTime.month,
        day = alignedDateTime.day,
        hour = alignedDateTime.hour,
        minute = 0,
        second = 0,
        nanosecond = 0,
    )
    val alignedInstant = aligned.toInstant(timeZone)
    return if (alignedInstant >= this) {
        alignedInstant
    } else {
        alignedInstant.plus(1.hours)
    }
}

private fun Instant.alignToDay(timeZone: TimeZone): Instant {
    val alignedDateTime = this.toLocalDateTime(timeZone)
    val aligned = LocalDateTime(
        year = alignedDateTime.year,
        month = alignedDateTime.month,
        day = alignedDateTime.day,
        hour = 0,
        minute = 0,
        second = 0,
        nanosecond = 0,
    )
    val alignedInstant = aligned.toInstant(timeZone)
    return if (alignedInstant >= this) {
        alignedInstant
    } else {
        alignedInstant.plus(1.hours)
    }
}
