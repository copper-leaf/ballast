package com.copperleaf.ballast.scheduler

import kotlin.time.Instant

public data class ScheduleEmission(
    val triggeredAt: Instant,
    val name: String,
    val schedule: Schedule,
)
