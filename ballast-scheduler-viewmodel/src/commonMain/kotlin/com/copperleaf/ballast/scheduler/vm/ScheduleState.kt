package com.copperleaf.ballast.scheduler.vm

import kotlin.time.Instant

public data class ScheduleState(
    val key: String?,
    val startedAt: Instant,
    val paused: Boolean = false,
    val firstUpdateAt: Instant? = null,
    val latestUpdateAt: Instant? = null,
    val numberOfDispatchedInputs: Int = 0,
)
