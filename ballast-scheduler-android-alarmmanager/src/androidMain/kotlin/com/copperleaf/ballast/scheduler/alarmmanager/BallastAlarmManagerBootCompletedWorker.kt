package com.copperleaf.ballast.scheduler.alarmmanager

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
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

        // Validate that this is actually a BOOT_COMPLETED intent to prevent spoofing
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) {
            Log.w("BallastAlarmManager", "Received intent with unexpected action: ${intent.action}")
            return
        }

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
        BallastAlarmManager.getAllConfigurations().forEach { ballastAlarmManager ->
            ballastAlarmManager.executor.synchronizeSchedules()
        }
    }
}
