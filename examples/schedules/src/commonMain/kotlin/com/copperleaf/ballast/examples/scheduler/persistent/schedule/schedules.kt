package com.copperleaf.ballast.examples.scheduler.persistent.schedule

import com.copperleaf.ballast.examples.scheduler.Notifications
import com.copperleaf.ballast.scheduler.NamedSchedule
import com.copperleaf.ballast.scheduler.SchedulerCallback
import com.copperleaf.ballast.scheduler.operators.named
import com.copperleaf.ballast.scheduler.schedule.EveryHourSchedule
import kotlinx.serialization.Serializable

@Serializable
class PersistentSchedule : NamedSchedule by EveryHourSchedule(5).named("PersistentSchedule")

@Serializable
class PersistentScheduleCallback : SchedulerCallback {
    override suspend fun handleTask() {
        Notifications().notify("Hourly Schedule", "From Ballast")
    }
}
