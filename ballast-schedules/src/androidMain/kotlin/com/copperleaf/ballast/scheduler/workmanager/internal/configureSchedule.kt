package com.copperleaf.ballast.scheduler.workmanager.internal

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkQuery
import com.copperleaf.ballast.scheduler.internal.RegisteredSchedule
import com.copperleaf.ballast.scheduler.workmanager.BallastWorkManagerData
import com.copperleaf.ballast.scheduler.workmanager.BallastWorkManagerScheduleWorker
import com.copperleaf.ballast.scheduler.workmanager.BallastWorkScheduleData
import kotlinx.datetime.Clock
import kotlin.time.toJavaDuration

@RequiresApi(Build.VERSION_CODES.O)
internal suspend fun WorkManager.configureSchedule(
    workData: BallastWorkManagerData,
    schedule: RegisteredSchedule<*, *, *>,
) {
    try {
        val query = WorkQuery.fromUniqueWorkNames(
            listOf("${workData.adapterClassName}::${schedule.key}")
        )
        val workInfoList = getWorkInfos(query).awaitInternal()
        if (workInfoList.isNotEmpty()) {
            // if there is already a scheduled task at this key, update it
            val workInfo = workInfoList.single()
            Log.d(
                "BallastWorkManager",
                "'${workData.adapterClassName}::${schedule.key}' current tags: ${workInfo.tags}"
            )
            updateExistingSchedule(
                BallastWorkScheduleData.fromWorkInfo(
                    workData,
                    workInfo,
                )
            )
            return
        }
    } catch (e: Exception) {
        // ignore
    }

    // This schedule has not been created before. Create a new one now
    createNewSchedule(workData, schedule)
}

@RequiresApi(Build.VERSION_CODES.O)
internal suspend fun WorkManager.createNewSchedule(
    workData: BallastWorkManagerData,
    schedule: RegisteredSchedule<*, *, *>,
) {
    val currentInstant = Clock.System.now()
    val scheduleData = BallastWorkScheduleData(
        workManagerData = workData,
        registeredSchedule = schedule,
        initialInstant = currentInstant,
        latestInstant = currentInstant,
    )

    if (scheduleData.nextInstant == null) {
        // schedule has completed, don't schedule another task
        Log.d("BallastWorkManager", "periodic work at '${scheduleData.key}' completed")
        return
    }

    val delayAmount = scheduleData.getDelayAmount(currentInstant)
    Log.d("BallastWorkManager", "creating new periodic work schedule at '${scheduleData.key}'")
    Log.d(
        "BallastWorkManager",
        "Scheduling next periodic work at '${scheduleData.key}' (to trigger at in $delayAmount at ${scheduleData.nextInstant})"
    )
    beginUniqueWork(
        /* uniqueWorkName = */ "${scheduleData.workManagerData.adapterClassName}::${scheduleData.key}",
        /* existingWorkPolicy = */ ExistingWorkPolicy.REPLACE,
        /* work = */ OneTimeWorkRequestBuilder<BallastWorkManagerScheduleWorker>()
            .addTag("ballast")
            .addTag("schedule")
            .also { workData.applyToWorkRequestBuilder(it) }
            .also { scheduleData.applyToWorkRequestBuilder(it) }
            .setInitialDelay(delayAmount.toJavaDuration())
            .build()
    )
        .enqueue()
}

@RequiresApi(Build.VERSION_CODES.O)
internal suspend fun WorkManager.updateExistingSchedule(
    scheduleData: BallastWorkScheduleData,
) {
    if (scheduleData.nextInstant == null) {
        // schedule has completed, don't schedule another task
        Log.d("BallastWorkManager", "periodic work at '${scheduleData.key}' completed")
        return
    }

    val delayAmount = scheduleData.getDelayAmount(Clock.System.now())

    Log.d("BallastWorkManager", "Updating existing periodic work schedule at '${scheduleData.key}'")
    Log.d(
        "BallastWorkManager",
        "Scheduling next periodic work at '${scheduleData.key}' (to trigger at in $delayAmount at ${scheduleData.nextInstant})"
    )
    beginUniqueWork(
        /* uniqueWorkName = */ "${scheduleData.workManagerData.adapterClassName}::${scheduleData.key}",
        /* existingWorkPolicy = */ ExistingWorkPolicy.REPLACE,
        /* work = */ OneTimeWorkRequestBuilder<BallastWorkManagerScheduleWorker>()
            .addTag("ballast")
            .addTag("schedule")
            .also { scheduleData.workManagerData.applyToWorkRequestBuilder(it) }
            .also { scheduleData.applyToWorkRequestBuilder(it) }
            .setInitialDelay(delayAmount.toJavaDuration())
            .build()
    )
        .enqueue()
}
