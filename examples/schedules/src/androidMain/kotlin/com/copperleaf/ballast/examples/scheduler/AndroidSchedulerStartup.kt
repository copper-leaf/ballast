package com.copperleaf.ballast.examples.scheduler

import android.content.Context
import android.util.Log
import androidx.startup.Initializer
import com.copperleaf.ballast.examples.scheduler.persistent.schedule.PersistentSchedule
import com.copperleaf.ballast.examples.scheduler.persistent.schedule.PersistentScheduleCallback
import com.copperleaf.ballast.scheduler.alarmmanager.AlarmManagerAdapter
import com.copperleaf.ballast.scheduler.alarmmanager.BallastAlarmManager
import com.copperleaf.ballast.scheduler.executor.event.EventDrivenScheduleExecutor

public class AndroidSchedulerStartup : Initializer<Unit> {
    override fun create(context: Context) {
        Log.d("BallastWorkManager", "Running AndroidSchedulerStartup")

//        executor = EventDrivenScheduleExecutor(
//            adapter = WorkManagerAdapter<PersistentSchedule, PersistentScheduleCallback>(
//                workManager = WorkManager.getInstance(context)
//            ),
//            scheduleSerializer = PersistentSchedule.serializer(),
//            callbackSerializer = PersistentScheduleCallback.serializer(),
//            state = PersistentScheduleState(),
//        )

        BallastAlarmManager.initialize(
            EventDrivenScheduleExecutor(
                adapter = AlarmManagerAdapter<PersistentSchedule, PersistentScheduleCallback>(context),
                scheduleSerializer = PersistentSchedule.serializer(),
                callbackSerializer = PersistentScheduleCallback.serializer(),
                state = PersistentScheduleState(),
            )
        )

        executor = BallastAlarmManager.getExecutor()
    }

    override fun dependencies(): List<Class<out Initializer<*>>> {
        return emptyList()
    }
}
