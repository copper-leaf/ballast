package com.copperleaf.ballast.examples.scheduler

import com.copperleaf.ballast.BallastLogger
import com.copperleaf.ballast.BallastViewModelConfiguration
import com.copperleaf.ballast.core.WasmJsConsoleLogger
import com.copperleaf.ballast.scheduler.executor.event.EventDrivenScheduleData
import com.copperleaf.ballast.scheduler.executor.event.EventDrivenScheduleExecutor

internal actual fun BallastViewModelConfiguration.Builder.installDebugger(): BallastViewModelConfiguration.Builder =
    apply {
        // debugger not yet available on wasmJs
    }

internal actual fun platformLogger(loggerName: String): BallastLogger {
    return WasmJsConsoleLogger(loggerName)
}

actual class Notifications actual constructor() {
    actual fun notify(
        title: String,
        message: String,
    ) { }

    actual fun getNotificationLogs(): List<String> {
        return emptyList()
    }
}

actual class PersistentScheduleState : EventDrivenScheduleExecutor.State {
    actual override suspend fun getAllSchedules(): Sequence<EventDrivenScheduleData> {
        return emptySequence()
    }
    actual override suspend fun getState(scheduleUniqueName: String): EventDrivenScheduleData? {
        return null
    }
    actual override suspend fun storeScheduleData(data: EventDrivenScheduleData) {
    }
    actual override suspend fun removeScheduleData(scheduleUniqueName: String) {
    }
}
