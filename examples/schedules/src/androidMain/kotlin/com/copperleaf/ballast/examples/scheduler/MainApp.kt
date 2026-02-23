package com.copperleaf.ballast.examples.scheduler

import android.app.Application
import androidx.work.Configuration
import com.copperleaf.ballast.scheduler.workmanager.BallastWorkManagerScheduleWorker

public class MainApp : Application(), Configuration.Provider {

    override fun onCreate() {
        INSTANCE = this
        super.onCreate()
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(BallastWorkManagerScheduleWorker.Factory({ executor }))
            .build()

    public companion object {
        var INSTANCE: MainApp? = null
    }
}
