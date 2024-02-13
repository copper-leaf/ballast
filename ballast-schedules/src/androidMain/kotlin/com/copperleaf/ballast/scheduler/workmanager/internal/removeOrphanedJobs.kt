package com.copperleaf.ballast.scheduler.workmanager.internal

import android.util.Log
import androidx.work.WorkManager
import com.copperleaf.ballast.scheduler.internal.RegisteredSchedule
import com.copperleaf.ballast.scheduler.workmanager.BallastWorkManagerData
import com.copperleaf.ballast.scheduler.workmanager.BallastWorkScheduleData
import kotlinx.coroutines.coroutineScope

internal suspend fun WorkManager.removeOrphanedJobs(
    workData: BallastWorkManagerData,
    schedules: List<RegisteredSchedule<*, *, *>>,
) {
    val adapterShortName = workData.adapterClassName.substringAfterLast('.')
    Log.d("BallastWorkManager", "Cleaning up orphaned jobs from $adapterShortName")

    coroutineScope {
        val allBallastJobs = getWorkInfosByTag("ballast").awaitInternal()

        // get all WorkManager jobs created by Ballast, across all adapters
        val currentScheduleNamesForThisAdapter = schedules.map { it.key }
        Log.d("BallastWorkManager", "${allBallastJobs.size} total Ballast jobs")

        // get the WorkManager jobs which were created specifically from this adapter
        val jobsForThisAdapter = allBallastJobs
            .filter {
                "schedule" in it.tags &&
                        "${BallastWorkManagerData.DATA_ADAPTER_CLASS}${workData.adapterClassName}" in it.tags
            }
        Log.d("BallastWorkManager", "${jobsForThisAdapter.size} jobs in $adapterShortName")

        jobsForThisAdapter.forEach {
            val scheduleKey = it.getStringFromTag(BallastWorkScheduleData.DATA_KEY)
            val isOrphaned = scheduleKey !in currentScheduleNamesForThisAdapter
            Log.d("BallastWorkManager", "    job[$adapterShortName::$scheduleKey]: isOrphaned=$isOrphaned")

            if (isOrphaned) {
                Log.d(
                    "BallastWorkManager",
                    "        Cancelling orphaned work schedule at '$$adapterShortName::$scheduleKey'"
                )
                cancelUniqueWork("${workData.adapterClassName}::$scheduleKey")
            }
        }
    }
}
