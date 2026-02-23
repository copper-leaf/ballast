package com.copperleaf.ballast.scheduler.alarmmanager

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.copperleaf.ballast.scheduler.Schedule
import com.copperleaf.ballast.scheduler.SchedulerCallback
import com.copperleaf.ballast.scheduler.alarmmanager.AlarmManagerConstants.KEY_INPUT_DATA_PAYLOAD
import com.copperleaf.ballast.scheduler.operators.getNext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlin.time.Clock

/**
 * This is job which executes on each tick of the registered schedule from AlarmManager, then enqueues the next Instant
 * that the job should rerun.
 */
@Suppress("UNCHECKED_CAST")
public class BallastAlarmManagerScheduleWorker : BroadcastReceiver() {

    private val clock: Clock = Clock.System
    private val json: Json = Json.Default

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) return

        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val scheduleData = getScheduleData(intent)
                dispatchWork(scheduleData)
                enqueueNextTask(context, scheduleData)
            } catch (e: Exception) {
                Log.e("BallastAlarmManager", "Error processing schedule", e)
            } finally {
                pendingResult.finish()
            }
        }
    }

    private suspend fun getScheduleData(intent: Intent): BallastAlarmManagerScheduleData {
        val payloadJson =
            intent.getStringExtra(KEY_INPUT_DATA_PAYLOAD) ?: error("Missing unique work name in input data")
        return json.decodeFromString(BallastAlarmManagerScheduleData.serializer(), payloadJson)
    }

    private suspend fun dispatchWork(scheduleData: BallastAlarmManagerScheduleData) {
        val adapter = createCallbackThroughReflection(scheduleData.callbackClassName)
        adapter.handleTask()
    }

    private suspend fun enqueueNextTask(context: Context, scheduleData: BallastAlarmManagerScheduleData) {
        val schedule = createScheduleThroughReflection(scheduleData.scheduleClassName)
        val next = schedule.getNext(clock.now())

        if (next != null) {
            context.updateExistingSchedule(
                scheduleData = scheduleData,
                runAt = next,
                json = json,
                clock = clock,
            )
        } else {
            context.cancelSchedule(
                schedule = schedule,
                json = json,
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
