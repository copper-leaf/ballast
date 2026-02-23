package com.copperleaf.ballast.scheduler.alarmmanager

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.copperleaf.ballast.scheduler.Schedule
import com.copperleaf.ballast.scheduler.SchedulerCallback
import com.copperleaf.ballast.scheduler.alarmmanager.state.PreferencesAlarmStateRepository
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
public class BallastAlarmManagerBootCompletedWorker : BroadcastReceiver() {

    private val clock: Clock = Clock.System
    private val json: Json = Json.Default

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) return

        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                restartAllAlarms(context)
            } finally {
                pendingResult.finish()
            }
        }
    }

    private suspend fun restartAllAlarms(context: Context) {
        val alarmStateRepository = PreferencesAlarmStateRepository(context, json)
        alarmStateRepository.getAllSchedules().forEach {
            val schedule = createScheduleThroughReflection(it.scheduleClassName)
            val callback = createCallbackThroughReflection(it.callbackClassName)

            context.createSchedule(schedule, callback, json, clock)
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
