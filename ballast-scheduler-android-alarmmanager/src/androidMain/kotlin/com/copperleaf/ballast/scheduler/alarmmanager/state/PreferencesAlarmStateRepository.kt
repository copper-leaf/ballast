package com.copperleaf.ballast.scheduler.alarmmanager.state

import android.content.Context
import kotlinx.serialization.json.Json

public class PreferencesAlarmStateRepository(
    private val context: Context,
    private val json: Json,
) : AlarmStateRepository {
    private val key = "ballast_alarm_manager_schedules"

    private val preferences = context.getSharedPreferences(key, Context.MODE_PRIVATE)

    override fun getAllSchedules(): List<AlarmState> {
        return getAndParseJsonState().alarms
    }

    override fun getStateForSchedule(scheduleClassName: String): AlarmState? {
        return getAndParseJsonState().alarms.find { it.scheduleClassName == scheduleClassName }
    }

    override fun setStateForSchedule(alarmState: AlarmState) {
        updateJsonState { state ->
            AlarmManagerState(
                state.alarms.toMutableList()
                    .apply {
                        removeAll { it.scheduleClassName == alarmState.scheduleClassName }
                        add(alarmState)
                    }
                    .toList()
            )
        }
    }

    override fun removeStateForSchedule(scheduleClassName: String) {
        updateJsonState { state ->
            AlarmManagerState(
                state.alarms.toMutableList()
                    .apply {
                        removeAll { it.scheduleClassName == scheduleClassName }
                    }
                    .toList()
            )
        }
    }

    private fun getAndParseJsonState(): AlarmManagerState {
        return preferences
            .getString(key, null)
            ?.let { json.decodeFromString(AlarmManagerState.serializer(), it) }
            ?: AlarmManagerState(emptyList())
    }


    private fun updateJsonState(block: (AlarmManagerState) -> AlarmManagerState) {
        val currentState = getAndParseJsonState()
        val newState = block(currentState)
        preferences.edit().putString(key, json.encodeToString(AlarmManagerState.serializer(), newState)).apply()
    }


}
