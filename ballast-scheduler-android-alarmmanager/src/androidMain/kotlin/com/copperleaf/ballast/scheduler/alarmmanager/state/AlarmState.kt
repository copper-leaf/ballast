package com.copperleaf.ballast.scheduler.alarmmanager.state

import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
public data class AlarmState(
    val scheduleClassName: String,
    val callbackClassName: String,
    val runAt: Instant,
)
