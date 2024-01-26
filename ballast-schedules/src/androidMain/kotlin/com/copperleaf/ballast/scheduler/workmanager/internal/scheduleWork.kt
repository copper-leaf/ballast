package com.copperleaf.ballast.scheduler.workmanager.internal

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.copperleaf.ballast.scheduler.SchedulerAdapter
import com.copperleaf.ballast.scheduler.workmanager.BallastWorkManagerData
import com.copperleaf.ballast.scheduler.workmanager.BallastWorkManagerScheduleDispatcher
import com.copperleaf.ballast.scheduler.workmanager.SchedulerCallback
import kotlin.time.Duration
import kotlin.time.toJavaDuration

@RequiresApi(Build.VERSION_CODES.O)
internal fun <I : Any, E : Any, S : Any> WorkManager.syncSchedulesOnStartupInternal(
    adapter: SchedulerAdapter<I, E, S>,
    callback: SchedulerCallback<I>,
    withHistory: Boolean,
) {
    val workData = BallastWorkManagerData(
        adapter = adapter,
        callback = callback,
        withHistory = withHistory,
    )
    Log.d("BallastWorkManager", "Scheduling work dispatch for ${workData.adapterClassName}")

    beginUniqueWork(
        /* uniqueWorkName = */ "${workData.adapterClassName} (on startup)",
        /* existingWorkPolicy = */ ExistingWorkPolicy.KEEP,
        /* work = */ OneTimeWorkRequestBuilder<BallastWorkManagerScheduleDispatcher>()
            .addTag("ballast")
            .addTag("dispatcher")
            .also { workData.applyToWorkRequestBuilder(it) }
            .build()
    ).enqueue()
}

@RequiresApi(Build.VERSION_CODES.O)
internal fun <I : Any, E : Any, S : Any> WorkManager.syncSchedulesPeriodicallyInternal(
    adapter: SchedulerAdapter<I, E, S>,
    callback: SchedulerCallback<I>,
    withHistory: Boolean,
    period: Duration,
) {
    val workData = BallastWorkManagerData(
        adapter = adapter,
        callback = callback,
        withHistory = withHistory,
    )
    Log.d("BallastWorkManager", "Scheduling work dispatch for ${workData.adapterClassName}")

    enqueueUniquePeriodicWork(
        /* uniqueWorkName = */ "${workData.adapterClassName} (periodic)",
        /* existingPeriodicWorkPolicy = */ ExistingPeriodicWorkPolicy.UPDATE,
        /* periodicWork = */ PeriodicWorkRequestBuilder<BallastWorkManagerScheduleDispatcher>(period.toJavaDuration())
            .addTag("ballast")
            .addTag("dispatcher")
            .also { workData.applyToWorkRequestBuilder(it) }
            .build()
    )
}
