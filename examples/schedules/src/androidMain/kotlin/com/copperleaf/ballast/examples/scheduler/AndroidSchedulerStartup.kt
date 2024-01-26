package com.copperleaf.ballast.examples.scheduler

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.startup.Initializer
import androidx.work.WorkManager
import androidx.work.WorkManagerInitializer
import com.copperleaf.ballast.scheduler.workmanager.syncSchedulesOnStartup

@RequiresApi(Build.VERSION_CODES.O)
public class AndroidSchedulerStartup : Initializer<Unit> {
    override fun create(context: Context) {
        Log.d("BallastWorkManager", "Running AndroidSchedulerStartup")
        WorkManager.getInstance(context)
            .syncSchedulesOnStartup(
                adapter = AndroidSchedulerExampleAdapter(),
                callback = AndroidSchedulerExampleCallback(),
                withHistory = false
            )
    }

    override fun dependencies(): List<Class<out Initializer<*>>> {
        return listOf(WorkManagerInitializer::class.java)
    }
}
