package com.copperleaf.ballast.examples.scheduler

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.startup.Initializer
import androidx.work.WorkManagerInitializer

@RequiresApi(Build.VERSION_CODES.O)
public class AndroidSchedulerStartup : Initializer<Unit> {
    override fun create(context: Context) {
        Log.d("BallastWorkManager", "Running AndroidSchedulerStartup")
    }

    override fun dependencies(): List<Class<out Initializer<*>>> {
        return listOf(WorkManagerInitializer::class.java)
    }
}
