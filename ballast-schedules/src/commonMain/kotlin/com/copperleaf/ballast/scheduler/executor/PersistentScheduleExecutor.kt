package com.copperleaf.ballast.scheduler.executor

/**
 * A [ScheduleExecutor] which executes a schedule even when the app is not currently running. The work is persistent and
 * will launch the application if it is not already running when the schedule needs to process its next task.
 */
public interface PersistentScheduleExecutor : ScheduleExecutor
