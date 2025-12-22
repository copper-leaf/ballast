package com.copperleaf.ballast.scheduler.utils

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

internal fun Instant.plusYears(value: Int, timeZone: TimeZone): Instant {
    val tDateTime = this.toLocalDateTime(timeZone)

    return LocalDateTime(
        year = tDateTime.year + value,
        month = tDateTime.month,
        day = tDateTime.day,
        hour = tDateTime.hour,
        minute = tDateTime.minute,
        second = tDateTime.second,
        nanosecond = tDateTime.nanosecond,
    ).toInstant(timeZone)
}

internal fun Instant.plusDays(value: Int, timeZone: TimeZone): Instant {
    return this.plus(value.days)
}

internal fun Instant.plusHours(value: Int, timeZone: TimeZone): Instant {
    return this.plus(value.hours)
}

internal fun Instant.plusMinutes(value: Int, timeZone: TimeZone): Instant {
    return this.plus(value.minutes)
}

internal fun Instant.plusSeconds(value: Int, timeZone: TimeZone): Instant {
    return this.plus(value.seconds)
}

internal fun Instant.withMonth(value: Int, timeZone: TimeZone): Instant {
    val tDateTime = this.toLocalDateTime(timeZone)

    return LocalDateTime(
        year = tDateTime.year,
        month = Month.entries[value - 1],
        day = tDateTime.day,
        hour = tDateTime.hour,
        minute = tDateTime.minute,
        second = tDateTime.second,
        nanosecond = tDateTime.nanosecond,
    ).toInstant(timeZone)
}

internal fun Instant.withDayOfMonth(value: Int, timeZone: TimeZone): Instant {
    val tDateTime = this.toLocalDateTime(timeZone)

    return LocalDateTime(
        year = tDateTime.year,
        month = tDateTime.month,
        day = value,
        hour = tDateTime.hour,
        minute = tDateTime.minute,
        second = tDateTime.second,
        nanosecond = tDateTime.nanosecond,
    ).toInstant(timeZone)
}

internal fun Instant.withHour(value: Int, timeZone: TimeZone): Instant {
    val tDateTime = this.toLocalDateTime(timeZone)

    return tDateTime.date.atTime(
        hour = value,
        minute = tDateTime.minute,
        second = tDateTime.second,
        nanosecond = tDateTime.nanosecond,
    ).toInstant(timeZone)
}

internal fun Instant.withMinute(value: Int, timeZone: TimeZone): Instant {
    val tDateTime = this.toLocalDateTime(timeZone)

    return tDateTime.date.atTime(
        hour = tDateTime.hour,
        minute = value,
        second = tDateTime.second,
        nanosecond = tDateTime.nanosecond,
    ).toInstant(timeZone)
}

internal fun Instant.withSecond(value: Int, timeZone: TimeZone): Instant {
    val tDateTime = this.toLocalDateTime(timeZone)

    return tDateTime.date.atTime(
        hour = tDateTime.hour,
        minute = tDateTime.minute,
        second = value,
        nanosecond = tDateTime.nanosecond,
    ).toInstant(timeZone)
}

internal fun Instant.withNano(value: Int, timeZone: TimeZone): Instant {
    val tDateTime = this.toLocalDateTime(timeZone)

    return tDateTime.date.atTime(
        hour = tDateTime.hour,
        minute = tDateTime.minute,
        second = tDateTime.second,
        nanosecond = value,
    ).toInstant(timeZone)
}
