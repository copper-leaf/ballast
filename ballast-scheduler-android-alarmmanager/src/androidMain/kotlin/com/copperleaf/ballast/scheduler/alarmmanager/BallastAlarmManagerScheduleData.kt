package com.copperleaf.ballast.scheduler.alarmmanager

import kotlinx.serialization.Serializable

@Serializable
public data class BallastAlarmManagerScheduleData(
    val scheduleClassName: String,
    val callbackClassName: String,
)
