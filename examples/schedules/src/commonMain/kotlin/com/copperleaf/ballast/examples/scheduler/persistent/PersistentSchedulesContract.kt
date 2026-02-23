package com.copperleaf.ballast.examples.scheduler.persistent

object PersistentSchedulesContract {
    data class State(
        val logs: List<String> = emptyList(),
    )

    sealed interface Inputs {
        object Initialize : Inputs
        object StartSchedule : Inputs
        object StopSchedule : Inputs
        object SendTestNotification : Inputs
    }

    sealed interface Events
}
