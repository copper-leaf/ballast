package com.copperleaf.ballast.scheduler.alarmmanager

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.copperleaf.ballast.scheduler.alarmmanager.AlarmManagerConstants.KEY_INPUT_DATA_PAYLOAD
import com.copperleaf.ballast.scheduler.executor.event.EventDrivenScheduleData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

@Suppress("UNCHECKED_CAST")
public class BallastAlarmManagerScheduleWorker : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) return

        val pendingResult = goAsync()

        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                onReceived(context, intent)
            } catch (e: Exception) {
                Log.e("BallastAlarmManager", "Error processing schedule", e)
            } finally {
                pendingResult.finish()
            }
        }
    }

    private suspend fun onReceived(context: Context, intent: Intent) {
        val payloadJson = intent.getStringExtra(KEY_INPUT_DATA_PAYLOAD) ?: error("Missing input data in extras")

        val ballastAlarmManager = BallastAlarmManager.getInstance()
        val executor = ballastAlarmManager.executor

        val data = executor.json.decodeFromString(EventDrivenScheduleData.serializer(), payloadJson)
        executor.handleTask(data)
    }
}
