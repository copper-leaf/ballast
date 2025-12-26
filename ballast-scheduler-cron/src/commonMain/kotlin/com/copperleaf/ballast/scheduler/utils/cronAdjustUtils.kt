package com.copperleaf.ballast.scheduler.utils

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlinx.datetime.number
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant


internal fun Instant.plusMinutes(value: Int, timeZone: TimeZone): Instant {
    return this.plus(value.minutes)
}

internal fun Instant.plusSeconds(value: Int, timeZone: TimeZone): Instant {
    return this.plus(value.seconds)
}

internal fun Instant.adjust(timeZone: TimeZone, block: LocalDateTime.() -> LocalDateTime): Instant {
    return this.toLocalDateTime(timeZone).block().toInstant(timeZone)
}

internal fun LocalDateTime.update(
    year: Int = this.year,
    month: Month = this.month,
    day: Int = this.day,
    hour: Int = this.hour,
    minute: Int = this.minute,
    second: Int = this.second,
    nanosecond: Int = this.nanosecond,
): LocalDateTime {
    return LocalDateTime(
        year = year,
        month = month,
        day = day,
        hour = hour,
        minute = minute,
        second = second,
        nanosecond = nanosecond,
    )
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

internal fun Instant.withMonth(month: Month, timeZone: TimeZone): Instant {
    return withMonth(month.number, timeZone)
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
