package com.copperleaf.ballast.scheduler.workmanager

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.work.CoroutineWorker
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.copperleaf.ballast.scheduler.Schedule
import com.copperleaf.ballast.scheduler.SchedulerCallback
import com.copperleaf.ballast.scheduler.operators.getNext
import com.copperleaf.ballast.scheduler.workmanager.WorkManagerConstants.KEY_INPUT_DATA_PAYLOAD
import kotlinx.coroutines.coroutineScope
import kotlinx.serialization.json.Json
import kotlin.time.Clock

/**
 * This is a WorkManager job which executes on each tick of the registered schedule, then enqueues the next Instant
 * that the job should rerun.
 */
@Suppress("UNCHECKED_CAST")
@RequiresApi(Build.VERSION_CODES.O)
public class BallastWorkManagerScheduleWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    private val clock: Clock = Clock.System
    private val json: Json = Json.Default

    final override suspend fun doWork(): Result = coroutineScope {
        val workManager = WorkManager.getInstance(applicationContext)

        val scheduleData = getScheduleData(workManager)
        dispatchWork(scheduleData)
        enqueueNextTask(workManager, scheduleData)

        Result.success()
    }

    private suspend fun getScheduleData(workManager: WorkManager): BallastWorkManagerScheduleData {
        val payloadJson = inputData.getString(KEY_INPUT_DATA_PAYLOAD) ?: error("Missing unique work name in input data")
        return json.decodeFromString(BallastWorkManagerScheduleData.serializer(), payloadJson)
    }

    private suspend fun dispatchWork(scheduleData: BallastWorkManagerScheduleData) {
        val adapter = createCallbackThroughReflection(scheduleData.callbackClassName)
        adapter.handleTask()
    }

    private suspend fun enqueueNextTask(workManager: WorkManager, scheduleData: BallastWorkManagerScheduleData) {
        val schedule = createScheduleThroughReflection(scheduleData.scheduleClassName)
        val next = schedule.getNext(clock.now())

        if (next != null) {
            workManager.updateExistingSchedule(
                scheduleData = scheduleData,
                runAt = next,
                json = json,
                clock = clock,
            )
        }
    }

    private fun createCallbackThroughReflection(className: String): SchedulerCallback {
        val callbackClass = Class.forName(className)
        return callbackClass.getDeclaredConstructor().newInstance() as SchedulerCallback
    }

    private fun createScheduleThroughReflection(className: String): Schedule {
        val callbackClass = Class.forName(className)
        return callbackClass.getDeclaredConstructor().newInstance() as Schedule
    }
}
