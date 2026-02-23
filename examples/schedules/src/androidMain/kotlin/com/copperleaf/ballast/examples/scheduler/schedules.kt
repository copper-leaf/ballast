package com.copperleaf.ballast.examples.scheduler

import com.copperleaf.ballast.scheduler.Schedule
import com.copperleaf.ballast.scheduler.schedule.EveryHourSchedule
import com.copperleaf.ballast.scheduler.workmanager.SchedulerCallback

class HourlySchedule : Schedule by EveryHourSchedule(0)
class HourlyCallback : SchedulerCallback {
    override suspend fun handleTask() {
        Notifications.notify("Hourly Schedule", "This notification is sent every hour on the hour")
    }
}
