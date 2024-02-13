package com.copperleaf.ballast.scheduler.vm

import kotlinx.datetime.Instant

public data class ScheduleState(
    val key: String,
    val startedAt: Instant,
    val paused: Boolean = false,
    val firstUpdateAt: Instant? = null,
    val latestUpdateAt: Instant? = null,
    val numberOfDispatchedInputs: Int = 0,
    val numberOfDroppedInputs: Int = 0,
    val numberOfFailedInputs: Int = 0,
)
