package com.copperleaf.ballast.scheduler.alarmmanager.state

public interface AlarmStateRepository {

    public fun getAllSchedules(): List<AlarmState>

    public fun getStateForSchedule(scheduleClassName: String): AlarmState?

    public fun setStateForSchedule(alarmState: AlarmState)

    public fun removeStateForSchedule(scheduleClassName: String)
}
