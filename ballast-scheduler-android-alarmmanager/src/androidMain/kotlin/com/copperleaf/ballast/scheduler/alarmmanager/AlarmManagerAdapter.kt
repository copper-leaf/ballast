package com.copperleaf.ballast.scheduler.alarmmanager

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.copperleaf.ballast.scheduler.NamedSchedule
import com.copperleaf.ballast.scheduler.SchedulerCallback
import com.copperleaf.ballast.scheduler.alarmmanager.AlarmManagerConstants.KEY_INPUT_DATA_PAYLOAD
import com.copperleaf.ballast.scheduler.executor.event.EventDrivenScheduleData
import com.copperleaf.ballast.scheduler.executor.event.EventDrivenScheduleExecutor
import kotlinx.serialization.json.Json

public class AlarmManagerAdapter<S : NamedSchedule, C : SchedulerCallback>(
    private val context: Context,
    private val json: Json = Json.Default,
) : EventDrivenScheduleExecutor.Adapter {
    override suspend fun registerSchedule(data: EventDrivenScheduleData) {
        val dataJson = json.encodeToString(EventDrivenScheduleData.serializer(), data)

        val ballastAlarmManagerConfiguration = BallastAlarmManager.getInstance(data.configuration)
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            data.scheduleUniqueName.hashCode(),
            Intent(context, BallastAlarmManagerScheduleWorker::class.java).apply {
                putExtra(KEY_INPUT_DATA_PAYLOAD, dataJson)
            },
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )

        when (ballastAlarmManagerConfiguration.precision) {
            AlarmPrecision.Low -> {
                alarmManager.set(
                    AlarmManager.RTC,
                    data.nextExecution.toEpochMilliseconds(),
                    pendingIntent,
                )
            }
            AlarmPrecision.Default -> {
                alarmManager.setExact(
                    AlarmManager.RTC,
                    data.nextExecution.toEpochMilliseconds(),
                    pendingIntent,
                )
            }
            AlarmPrecision.High -> {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    data.nextExecution.toEpochMilliseconds(),
                    pendingIntent,
                )
            }
        }
    }

    override suspend fun updateSchedule(data: EventDrivenScheduleData) {
        registerSchedule(data)
    }

    override suspend fun cancelSchedule(data: EventDrivenScheduleData) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(
            PendingIntent.getBroadcast(
                context,
                data.scheduleUniqueName.hashCode(),
                Intent(context, BallastAlarmManagerScheduleWorker::class.java),
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
        )
    }

    override suspend fun synchronizeSchedules(schedules: Sequence<EventDrivenScheduleData>) {
        schedules.forEach {
            registerSchedule(it)
        }
    }
}
