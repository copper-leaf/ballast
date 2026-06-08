package com.copperleaf.ballast.examples.presentation.utils

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

fun clockFlow(
    clock: Clock,
    timeZone: TimeZone,
): Flow<Instant> {
    return flow {
        while (true) {
            val now = clock.now()
            val nextSecond = now.alignToNextSecond(timeZone)
            val delayUntilNextSeconds = nextSecond - now

            emit(now)
            delay(delayUntilNextSeconds)
        }
    }
}

private fun Instant.alignToNextSecond(timeZone: TimeZone): Instant {
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

val Instant.formatted: String get() {
    return this.toLocalDateTime(TimeZone.currentSystemDefault()).time.let {
        "${it.hour}:${it.minute.toString().padStart(2, '0')}:${it.second.toString().padStart(2, '0')}"
    }
}

val Duration.formatted: String get() {
    return this.inWholeSeconds.seconds.toString()
}
