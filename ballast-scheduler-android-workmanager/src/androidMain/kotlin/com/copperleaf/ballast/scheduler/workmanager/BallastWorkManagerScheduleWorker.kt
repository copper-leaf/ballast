package com.copperleaf.ballast.scheduler.workmanager

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import com.copperleaf.ballast.scheduler.NamedSchedule
import com.copperleaf.ballast.scheduler.SchedulerCallback
import com.copperleaf.ballast.scheduler.executor.event.EventDrivenScheduleData
import com.copperleaf.ballast.scheduler.executor.event.EventDrivenScheduleExecutor
import com.copperleaf.ballast.scheduler.workmanager.WorkManagerConstants.KEY_INPUT_DATA_PAYLOAD
import kotlinx.coroutines.coroutineScope

public class BallastWorkManagerScheduleWorker<S : NamedSchedule, C : SchedulerCallback>(
    context: Context,
    workerParams: WorkerParameters,
    private val executor: EventDrivenScheduleExecutor<S, C>,
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result = coroutineScope {
        val payloadJson = inputData.getString(KEY_INPUT_DATA_PAYLOAD) ?: error("Missing input data payload")
        val scheduleData = executor.json.decodeFromString(EventDrivenScheduleData.serializer(), payloadJson)

        executor.handleTask(scheduleData)

        Result.success()
    }

    public class Factory(
        private val executor: () -> EventDrivenScheduleExecutor<*, *>?,
    ) : WorkerFactory() {

        override fun createWorker(
            appContext: Context,
            workerClassName: String,
            workerParameters: WorkerParameters
        ): ListenableWorker? {
            Log.i("BallastWorkManager", "Factory called to create worker of type '$workerClassName'")
            if (workerClassName == BallastWorkManagerScheduleWorker::class.java.name) {
                executor()?.let {
                    return BallastWorkManagerScheduleWorker(appContext, workerParameters, it)
                }
            }

            return null
        }
    }
}
