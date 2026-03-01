package com.copperleaf.ballast.scheduler.executor.event

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import kotlin.time.Instant

@Serializable
public data class EventDrivenScheduleData(
    val configuration: String?,
    val scheduleUniqueName: String,
    val scheduleJson: JsonObject,
    val callbackJson: JsonObject,
    val lastExecution: Instant?,
    val nextExecution: Instant,
)
