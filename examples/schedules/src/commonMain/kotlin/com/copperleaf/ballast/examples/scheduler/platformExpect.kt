@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package com.copperleaf.ballast.examples.scheduler

import com.copperleaf.ballast.BallastLogger
import com.copperleaf.ballast.BallastViewModelConfiguration
import com.copperleaf.ballast.examples.scheduler.persistent.schedule.PersistentSchedule
import com.copperleaf.ballast.examples.scheduler.persistent.schedule.PersistentScheduleCallback
import com.copperleaf.ballast.scheduler.executor.event.EventDrivenScheduleExecutor

internal expect fun BallastViewModelConfiguration.Builder.installDebugger(): BallastViewModelConfiguration.Builder

internal expect fun platformLogger(loggerName: String): BallastLogger

var executor: EventDrivenScheduleExecutor<PersistentSchedule, PersistentScheduleCallback>? = null

expect class Notifications() {
    fun notify(
        title: String,
        message: String,
    )

    fun getNotificationLogs(): List<String>
}
