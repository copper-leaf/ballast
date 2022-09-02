package com.copperleaf.ballast.debugger.utils

import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration

public fun LocalDateTime.Companion.now(): LocalDateTime {
    return Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
}

public operator fun LocalDateTime.minus(other: LocalDateTime): Duration {
    return this.toInstant(TimeZone.currentSystemDefault()) - other.toInstant(TimeZone.currentSystemDefault())
}

public fun Duration.removeFraction(minUnit: DurationUnit): Duration {
    for (unit in DurationUnit.values().reversed()) {
        val wholeNumberInUnit = this.toLong(unit)

        if (wholeNumberInUnit > 0) return wholeNumberInUnit.toDuration(unit)
        if (unit == minUnit) break
    }

    return this.toLong(minUnit).toDuration(minUnit)
}
