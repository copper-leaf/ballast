package com.copperleaf.ballast.scheduler.alarmmanager

public enum class AlarmPrecision {
    /**
     * Sets alarms using [android.app.AlarmManager.set]. Intended for low-priority background work that is not visible
     * to end users and doesn't need to be exact, and can be deferred by the system in order to optimize battery life.
     * Alarms set with this method will not wake the device if it is asleep.
     *
     * Best for: background synchronization, database/cache maintenance
     */
    Low,

    /**
     * Sets alarms using [android.app.AlarmManager.setExact]. Intended for user-facing features that require exact
     * timing, but do not necessarily need to wake the device if it is asleep. Alarms set with this method will be
     * delivered at approximately the exact time specified, but may be deferred if the device is asleep. Alarms
     * triggered while the device is asleep will be delivered as soon as the device wakes up.
     *
     * Best for: marketing notifications, non-urgent reminders
     */
    Default,

    /**
     * Sets alarms using [android.app.AlarmManager.setExactAndAllowWhileIdle]. Intended for user-facing features that
     * require exact timing and need to be delivered even if the device is asleep. Alarms set with this method will
     * wake up the device to send the notification at the exact time specified, Use this option sparingly, as it can
     * have a significant impact on battery life.
     *
     * Best for: time-sensitive notifications, calendar events
     */
    High,
}
