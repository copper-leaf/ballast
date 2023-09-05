package com.copperleaf.ballast.debugger.utils

import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.microseconds
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.nanoseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.time.DurationUnit

public fun LocalDateTime.Companion.now(): LocalDateTime {
    return Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
}

public operator fun LocalDateTime.minus(other: LocalDateTime): Duration {
    return this.toInstant(TimeZone.currentSystemDefault()) - other.toInstant(TimeZone.currentSystemDefault())
}

public fun Duration.removeFraction(minUnit: DurationUnit): Duration {
    return when(minUnit) {
        DurationUnit.NANOSECONDS -> this.inWholeNanoseconds.nanoseconds
        DurationUnit.MICROSECONDS -> this.inWholeMicroseconds.microseconds
        DurationUnit.MILLISECONDS -> this.inWholeMilliseconds.milliseconds
        DurationUnit.SECONDS -> this.inWholeSeconds.seconds
        DurationUnit.MINUTES -> this.inWholeMinutes.minutes
        DurationUnit.HOURS -> this.inWholeHours.hours
        DurationUnit.DAYS -> this.inWholeDays.days
        else -> this.inWholeSeconds.seconds
    }
}
