package com.copperleaf.ballast.examples.scheduler

import com.copperleaf.ballast.BallastLogger
import com.copperleaf.ballast.BallastViewModelConfiguration
import com.copperleaf.ballast.core.OSLogLogger
import com.copperleaf.ballast.debugger.BallastDebuggerClientConnection
import com.copperleaf.ballast.debugger.BallastDebuggerInterceptor
import com.copperleaf.ballast.plusAssign
import com.copperleaf.ballast.scheduler.executor.event.EventDrivenScheduleData
import com.copperleaf.ballast.scheduler.executor.event.EventDrivenScheduleExecutor
import io.ktor.client.engine.darwin.Darwin
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlin.experimental.ExperimentalNativeApi

private val lazyConnection by lazy {
    BallastDebuggerClientConnection(
        engineFactory = Darwin,
        applicationCoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
        host = "127.0.0.1",
    ) {
        // CIO Ktor client engine configuration
    }.also { it.connect() }
}

internal actual fun BallastViewModelConfiguration.Builder.installDebugger(): BallastViewModelConfiguration.Builder =
    apply {
        this += BallastDebuggerInterceptor(lazyConnection)
    }

@OptIn(ExperimentalForeignApi::class, ExperimentalNativeApi::class)
internal actual fun platformLogger(loggerName: String): BallastLogger {
    return OSLogLogger(loggerName)
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
