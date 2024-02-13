package com.copperleaf.ballast.scheduler.workmanager

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.work.CoroutineWorker
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.copperleaf.ballast.scheduler.SchedulerAdapter
import com.copperleaf.ballast.scheduler.workmanager.internal.configureSchedule
import com.copperleaf.ballast.scheduler.workmanager.internal.getRegisteredSchedules
import com.copperleaf.ballast.scheduler.workmanager.internal.removeOrphanedJobs
import kotlinx.coroutines.coroutineScope

/**
 * This is a WorkManager job which takes a [SchedulerAdapter] and creates new WorkManager tasks for each registered
 * schedule. Those are represented by OneTimeJobs which schedule their next execution moment after each subsequent
 * execution.
 *
 * The newly scheduled jobs are instances of [BallastWorkManagerScheduleWorker] which run the current execution, and
 * then schedule the next execution.
 */
@Suppress("UNCHECKED_CAST")
@RequiresApi(Build.VERSION_CODES.O)
public class BallastWorkManagerScheduleDispatcher(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result = coroutineScope {
        val workManager = WorkManager.getInstance(applicationContext)

        val workData: BallastWorkManagerData = BallastWorkManagerData.fromListenableWorker(this@BallastWorkManagerScheduleDispatcher)
        Log.d("BallastWorkManager", "Handling work dispatch: $workData")

        val schedules = workData.adapter.getRegisteredSchedules()

        // Make sure each registered schedule is set up
        schedules.forEach { schedule ->
            workManager.configureSchedule(
                workData = workData,
                schedule = schedule,
            )
        }

        // remove schedules which are not part of the current adapter's schedule
        workManager.removeOrphanedJobs(
            workData = workData,
            schedules = schedules,
        )

        Result.success()
    }
}
