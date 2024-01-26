package com.copperleaf.ballast.examples.scheduler

import kotlinx.datetime.LocalDateTime
import kotlin.time.Duration

object SchedulerExampleContract {
    data class State(
        val count: Int = 0,
        val scheduledUpdateTimes: List<Pair<String, LocalDateTime>> = emptyList(),
    )

    sealed interface Inputs {
        data class Increment(val scheduleKey: String, val amount: Int, val processingTime: Duration = Duration.ZERO) : Inputs

        data object StartSchedules : Inputs
        data class PauseSchedule(val key: String) : Inputs
        data class ResumeSchedule(val key: String) : Inputs
        data class StopSchedule(val key: String) : Inputs
    }

    sealed interface Events
}
