package com.copperleaf.ballast.scheduler.alarmmanager

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import com.copperleaf.ballast.scheduler.executor.event.EventDrivenScheduleData
import com.copperleaf.ballast.scheduler.executor.event.EventDrivenScheduleExecutor
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

public class SharedPreferencesScheduleState(
    private val preferences: SharedPreferences
) : EventDrivenScheduleExecutor.State {

    public constructor(applicationContext: Context) : this(
        applicationContext.getSharedPreferences("schedules", MODE_PRIVATE)
    )

    private val json: Json = Json.Default
    private val serializer = ListSerializer(EventDrivenScheduleData.serializer())

    private var scheduleState: List<EventDrivenScheduleData>
        get() = preferences.getString("scheduleState", null)
            ?.let { json.decodeFromString(serializer, it) }
            ?: emptyList()
        set(value) {
            preferences
                .edit()
                .putString("scheduleState", json.encodeToString(serializer, value))
                .apply()
        }

    override suspend fun getAllSchedules(): Sequence<EventDrivenScheduleData> {
        return scheduleState.asSequence()
    }

    override suspend fun getState(scheduleUniqueName: String): EventDrivenScheduleData? {
        return scheduleState.find { it.scheduleUniqueName == scheduleUniqueName }
    }

    override suspend fun storeScheduleData(data: EventDrivenScheduleData) {
        val existing = scheduleState.find { it.scheduleUniqueName == data.scheduleUniqueName }
        if (existing != null) {
            scheduleState = scheduleState - existing + data
        } else {
            scheduleState = scheduleState + data
        }
    }

    override suspend fun removeScheduleData(scheduleUniqueName: String) {
        scheduleState = scheduleState.filterNot { it.scheduleUniqueName == scheduleUniqueName }
    }
}
