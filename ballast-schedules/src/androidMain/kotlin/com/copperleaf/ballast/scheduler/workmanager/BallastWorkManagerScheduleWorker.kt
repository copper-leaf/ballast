package com.copperleaf.ballast.scheduler.workmanager

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.work.CoroutineWorker
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.copperleaf.ballast.scheduler.executor.ScheduleExecutor
import com.copperleaf.ballast.scheduler.internal.RegisteredSchedule
import com.copperleaf.ballast.scheduler.workmanager.internal.updateExistingSchedule
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext

/**
 * This is a WorkManager job which executes on each tick of the registered schedule, then enqueues the next Instant
 * that the job should rerun. It also is responsible for accessing the target VM that the Input should be sent to on
 * each task tick.
 */
@Suppress("UNCHECKED_CAST")
@RequiresApi(Build.VERSION_CODES.O)
public class BallastWorkManagerScheduleWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    final override suspend fun doWork(): Result = coroutineScope {
        // unpack data from the work input data, and create adapter and callback classes with reflection
        val workManager = WorkManager.getInstance(applicationContext)

        val workData = BallastWorkManagerData.fromListenableWorker(this@BallastWorkManagerScheduleWorker)
        val scheduleData = BallastWorkScheduleData.fromListenableWorker(workData, this@BallastWorkManagerScheduleWorker)

        Log.d("BallastWorkManager", "running periodic job at '${scheduleData.key}'")
        dispatchWork(workManager, scheduleData)

        Result.success()
    }

    internal companion object {
        internal suspend fun dispatchWork(
            workManager: WorkManager,
            scheduleData: BallastWorkScheduleData,
        ) {
            when (scheduleData.registeredSchedule.delayMode) {
                ScheduleExecutor.DelayMode.FireAndForget -> {
                    workManager.updateExistingSchedule(
                        scheduleData = scheduleData,
                    )
                    dispatchWork(
                        callback = scheduleData.workManagerData.callback,
                        registeredSchedule = scheduleData.registeredSchedule,
                        deferred = null,
                    )
                }

                ScheduleExecutor.DelayMode.Suspend -> {
                    val deferred = CompletableDeferred<Unit>()
                    dispatchWork(
                        callback = scheduleData.workManagerData.callback,
                        registeredSchedule = scheduleData.registeredSchedule,
                        deferred = deferred,
                    )
                    deferred.await()
                    workManager.updateExistingSchedule(
                        scheduleData = scheduleData,
                    )
                }
            }
        }

        private suspend fun dispatchWork(
            callback: SchedulerCallback<*>,
            registeredSchedule: RegisteredSchedule<*, *, *>,
            deferred: CompletableDeferred<Unit>?
        ) {
            invokeWith(
                callback,
                registeredSchedule.scheduledInput()
            )

            deferred?.complete(Unit)
        }

        private suspend fun <P1> invokeWith(
            fn: SchedulerCallback<P1>,
            input: Any,
        ) {
            withContext(Dispatchers.Main) {
                fn.dispatchInput(input as P1)
            }
        }
    }
}
