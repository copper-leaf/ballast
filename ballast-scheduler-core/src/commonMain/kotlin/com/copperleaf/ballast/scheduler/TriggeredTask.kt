package com.copperleaf.ballast.scheduler

import kotlin.time.Instant

public data class TriggeredTask(
    val triggeredAt: Instant,
    val name: String?,
    val schedule: Schedule,
)
