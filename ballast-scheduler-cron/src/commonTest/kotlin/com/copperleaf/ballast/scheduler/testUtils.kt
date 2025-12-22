package com.copperleaf.ballast.scheduler

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant

fun Sequence<Instant>.firstTen(timeZone: TimeZone): List<LocalDateTime> {
    return this
        .map { it.toLocalDateTime(timeZone) }
        .take(10)
        .toList()
}
