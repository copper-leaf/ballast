package com.copperleaf.ballast.examples.scheduler.persistent

import com.copperleaf.ballast.InputHandler
import com.copperleaf.ballast.InputHandlerScope
import com.copperleaf.ballast.examples.scheduler.Notifications
import com.copperleaf.ballast.examples.scheduler.persistent.schedule.PersistentSchedule
import com.copperleaf.ballast.examples.scheduler.persistent.schedule.PersistentScheduleCallback
import com.copperleaf.ballast.scheduler.executor.event.EventDrivenScheduleExecutor

class PersistentSchedulesInputHandler(
    private val executor: EventDrivenScheduleExecutor<PersistentSchedule, PersistentScheduleCallback>,
    private val notifications: Notifications,
) : InputHandler<
        PersistentSchedulesContract.Inputs,
        PersistentSchedulesContract.Events,
        PersistentSchedulesContract.State> {
    override suspend fun InputHandlerScope<
            PersistentSchedulesContract.Inputs,
            PersistentSchedulesContract.Events,
            PersistentSchedulesContract.State>.handleInput(
        input: PersistentSchedulesContract.Inputs
    ): Unit = when (input) {
        is PersistentSchedulesContract.Inputs.Initialize -> {
            updateState { it.copy(logs = notifications.getNotificationLogs()) }
        }

        is PersistentSchedulesContract.Inputs.StartSchedule -> {
            sideJob("startSchedule") {
                executor.registerSchedule(PersistentSchedule(), PersistentScheduleCallback())
            }
        }

        is PersistentSchedulesContract.Inputs.StopSchedule -> {
            sideJob("StopSchedule") {
                executor.cancelSchedule(PersistentSchedule())
            }
        }

        is PersistentSchedulesContract.Inputs.SendTestNotification -> {
            notifications.notify("Test Notification", "From Ballast")
            updateState { it.copy(logs = notifications.getNotificationLogs()) }
        }
    }
}
