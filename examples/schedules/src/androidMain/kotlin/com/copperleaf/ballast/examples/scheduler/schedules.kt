package com.copperleaf.ballast.examples.scheduler

import com.copperleaf.ballast.scheduler.Schedule
import com.copperleaf.ballast.scheduler.SchedulerCallback
import com.copperleaf.ballast.scheduler.schedule.EveryHourSchedule

class WorkManagerSchedule : Schedule by EveryHourSchedule(0, 10, 20, 30, 40, 50)
class WorkManagerCallback : SchedulerCallback {
    override suspend fun handleTask() {
        Notifications.notify("Hourly Schedule", "From WorkManager")
    }
}

class AlarmManagerSchedule : Schedule by EveryHourSchedule(5, 15, 25, 35, 45, 55)
class AlarmManagerCallback : SchedulerCallback {
    override suspend fun handleTask() {
        Notifications.notify("Every Minute Schedule", "From AlarmManager")
    }
}
