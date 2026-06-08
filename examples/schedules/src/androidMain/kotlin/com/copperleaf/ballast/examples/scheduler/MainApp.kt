package com.copperleaf.ballast.examples.scheduler

import android.app.Application
import com.copperleaf.ballast.examples.scheduler.persistent.schedule.PersistentSchedule
import com.copperleaf.ballast.examples.scheduler.persistent.schedule.PersistentScheduleCallback
import com.copperleaf.ballast.scheduler.alarmmanager.AlarmManagerAdapter
import com.copperleaf.ballast.scheduler.alarmmanager.AlarmPrecision
import com.copperleaf.ballast.scheduler.alarmmanager.BallastAlarmManager
import com.copperleaf.ballast.scheduler.alarmmanager.SharedPreferencesScheduleState
import com.copperleaf.ballast.scheduler.executor.event.EventDrivenScheduleExecutor

public class MainApp : Application() {

    override fun onCreate() {
        INSTANCE = this
        super.onCreate()

        executor = BallastAlarmManager.initialize(
            EventDrivenScheduleExecutor(
                adapter = AlarmManagerAdapter<PersistentSchedule, PersistentScheduleCallback>(this),
                scheduleSerializer = PersistentSchedule.serializer(),
                callbackSerializer = PersistentScheduleCallback.serializer(),
                state = SharedPreferencesScheduleState(this),
            ),
            precision = AlarmPrecision.High,
        )
    }

    public companion object {
        var INSTANCE: MainApp? = null
    }
}
