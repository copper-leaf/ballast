package com.copperleaf.ballast.scheduler.workmanager

import kotlinx.serialization.Serializable

@Serializable
public data class BallastWorkManagerScheduleData(
    val scheduleClassName: String,
    val callbackClassName: String,
)
