package com.copperleaf.ballast.scheduler

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant

fun Sequence<Instant>.firstTen(timeZone: TimeZone = TimeZone.UTC): List<LocalDateTime> {
    return this
        .map { it.toLocalDateTime(timeZone) }
        .take(10)
        .toList()
}

suspend fun Flow<ScheduleEmission>.firstTen(timeZone: TimeZone = TimeZone.UTC): List<LocalDateTime> {
    return this
        .map { it.triggeredAt.toLocalDateTime(timeZone) }
        .take(10)
        .toList()
}

suspend fun Flow<ScheduleEmission>.firstTenWithNames(timeZone: TimeZone = TimeZone.UTC): List<Pair<String, LocalDateTime>> {
    return this
        .map { it.name to it.triggeredAt.toLocalDateTime(timeZone) }
        .take(10)
        .toList()
}
