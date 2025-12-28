package com.copperleaf.ballast.scheduler.utils

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant

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
