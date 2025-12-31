package com.copperleaf.ballast.scheduler.utils

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

internal fun Instant.isSameOrBeforeMinute(other: Instant, timeZone: TimeZone): Boolean {
    val a = this.alignToNextMinute(timeZone)
    val b = other.alignToNextMinute(timeZone)
    return a <= b
}

internal fun Instant.alignToNextSecond(timeZone: TimeZone): Instant {
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

internal fun Instant.alignToNextMinute(timeZone: TimeZone): Instant {
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

internal fun Instant.alignToNextHour(timeZone: TimeZone): Instant {
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

internal fun Instant.alignToNextDay(timeZone: TimeZone): Instant {
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
        alignedInstant.plus(1.days)
    }
}
