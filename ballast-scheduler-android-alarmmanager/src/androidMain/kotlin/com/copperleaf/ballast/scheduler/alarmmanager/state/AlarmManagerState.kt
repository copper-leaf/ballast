package com.copperleaf.ballast.scheduler.alarmmanager.state

import kotlinx.serialization.Serializable

@Serializable
public data class AlarmManagerState(
    val alarms: List<AlarmState>
)
