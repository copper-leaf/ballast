package com.copperleaf.ballast.examples.scheduler

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.startup.Initializer
import androidx.work.WorkManager
import androidx.work.WorkManagerInitializer
import com.copperleaf.ballast.scheduler.alarmmanager.createSchedule
import com.copperleaf.ballast.scheduler.workmanager.createSchedule

@RequiresApi(Build.VERSION_CODES.O)
public class AndroidSchedulerStartup : Initializer<Unit> {
    override fun create(context: Context) {
        Log.d("BallastWorkManager", "Running AndroidSchedulerStartup")

        val workManager = WorkManager.getInstance(context)

        Notifications.notify(
            title = "Ballast Scheduler",
            message = "App Launch",
            context = context
        )

        workManager.createSchedule(
            schedule = WorkManagerSchedule(),
            callback = WorkManagerCallback()
        )
        context.createSchedule(
            schedule = AlarmManagerSchedule(),
            callback = AlarmManagerCallback()
        )
    }

    override fun dependencies(): List<Class<out Initializer<*>>> {
        return listOf(WorkManagerInitializer::class.java)
    }
}
