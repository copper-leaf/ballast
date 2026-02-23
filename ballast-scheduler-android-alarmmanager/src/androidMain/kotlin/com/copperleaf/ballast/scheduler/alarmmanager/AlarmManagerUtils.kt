package com.copperleaf.ballast.scheduler.alarmmanager

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import com.copperleaf.ballast.scheduler.Schedule
import com.copperleaf.ballast.scheduler.SchedulerCallback
import com.copperleaf.ballast.scheduler.alarmmanager.AlarmManagerConstants.KEY_INPUT_DATA_PAYLOAD
import com.copperleaf.ballast.scheduler.alarmmanager.state.AlarmState
import com.copperleaf.ballast.scheduler.alarmmanager.state.AlarmStateRepository
import com.copperleaf.ballast.scheduler.alarmmanager.state.PreferencesAlarmStateRepository
import com.copperleaf.ballast.scheduler.operators.getNext
import kotlinx.serialization.json.Json
import kotlin.time.Clock
import kotlin.time.Instant

public fun Context.createSchedule(
    schedule: Schedule,
    callback: SchedulerCallback,
    json: Json = Json.Default,
    clock: Clock = Clock.System,
    alarmStateRepository: AlarmStateRepository = PreferencesAlarmStateRepository(this, json)
) {
    val scheduleData = BallastAlarmManagerScheduleData(
        scheduleClassName = schedule::class.qualifiedName!!,
        callbackClassName = callback::class.qualifiedName!!,
    )
    val payloadJson = json.encodeToString(BallastAlarmManagerScheduleData.serializer(), scheduleData)

    val existingState = alarmStateRepository.getStateForSchedule(schedule::class.qualifiedName!!)

    val runAt = if (existingState == null) {
        schedule.getNext(clock.now()) ?: run {
            Log.i(
                "BallastWorkManager",
                "Schedule ${schedule::class.qualifiedName} has no next run time, skipping creation"
            )
            return
        }
    } else {
        existingState.runAt
    }

    val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

    val pendingIntent = PendingIntent.getBroadcast(
        this,
        schedule::class.qualifiedName!!.hashCode(),
        Intent(this, BallastAlarmManagerScheduleWorker::class.java).apply {
            putExtra(KEY_INPUT_DATA_PAYLOAD, payloadJson)
        },
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
    )

    alarmManager.setExact(
        AlarmManager.RTC_WAKEUP,
        runAt.toEpochMilliseconds(),
        pendingIntent,
    )

    alarmStateRepository.setStateForSchedule(
        AlarmState(
            scheduleClassName = schedule::class.qualifiedName!!,
            callbackClassName = callback::class.qualifiedName!!,
            runAt = runAt,
        )
    )
}

internal suspend fun Context.updateExistingSchedule(
    scheduleData: BallastAlarmManagerScheduleData,
    runAt: Instant,
    json: Json = Json.Default,
    clock: Clock = Clock.System,
    alarmStateRepository: AlarmStateRepository = PreferencesAlarmStateRepository(this, json)
) {
    val payloadJson = json.encodeToString(BallastAlarmManagerScheduleData.serializer(), scheduleData)
    val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

    val pendingIntent = PendingIntent.getBroadcast(
        this,
        scheduleData.scheduleClassName.hashCode(),
        Intent(this, BallastAlarmManagerScheduleWorker::class.java).apply {
            putExtra(KEY_INPUT_DATA_PAYLOAD, payloadJson)
        },
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
    )

    alarmManager.setExact(
        AlarmManager.RTC_WAKEUP,
        runAt.toEpochMilliseconds(),
        pendingIntent,
    )

    alarmStateRepository.setStateForSchedule(
        AlarmState(
            scheduleClassName = scheduleData.scheduleClassName,
            callbackClassName = scheduleData.callbackClassName,
            runAt = runAt,
        )
    )
}

public suspend fun Context.cancelSchedule(
    schedule: Schedule,
    json: Json = Json.Default,
    alarmStateRepository: AlarmStateRepository = PreferencesAlarmStateRepository(this, json)
) {
    val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
    alarmManager.cancel(
        PendingIntent.getBroadcast(
            this,
            schedule::class.qualifiedName!!.hashCode(),
            Intent(this, BallastAlarmManagerScheduleWorker::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    )

    alarmStateRepository.removeStateForSchedule(schedule::class.qualifiedName!!)
}
