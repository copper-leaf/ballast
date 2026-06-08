package com.copperleaf.ballast.scheduler

import kotlinx.coroutines.flow.Flow

public interface ScheduleExecutor {
    /**
     * Executes a single [NamedSchedule], producing a [Flow] of [TriggeredTask] events indicating tasks to be
     * completed. Tasks should be fully handled directly in the flow if you wish to apply backpressure to the schedule
     * emissions, dropping emissions from the upstream schedule that would have been emitted while the previous task
     * was still being handled.
     *
     * All [TriggeredTask] emitted by this Flow will have a `null` name, even if the [Schedule] is actually a [NamedSchedule].
     */
    public fun runSchedule(schedule: Schedule): Flow<TriggeredTask>

    /**
     * Executes a single [NamedSchedule], producing a [Flow] of [TriggeredTask] events indicating tasks to be
     * completed. Tasks should be fully handled directly in the flow if you wish to apply backpressure to the schedule
     * emissions, dropping emissions from the upstream schedule that would have been emitted while the previous task
     * was still being handled.
     */
    public fun runSchedule(schedule: NamedSchedule): Flow<TriggeredTask>

    /**
     * Executes multiple [NamedSchedule]s, producing a [Flow] of [TriggeredTask] events indicating tasks to be
     * completed. Tasks from all schedules with be merged into one with the [kotlinx.coroutines.flow.merge] operator,
     * which does not allow backpressure to be applied to the individual schedule's original upstream flow (since
     * backpressure would block all schedules, not just the slow one). Therefore, it is best to use this executor to
     * dispatch the scheduled tasks to another system that can handle backpressure, such as Ballast Queue.
     */
    public fun runSchedules(schedules: List<NamedSchedule>): Flow<TriggeredTask>

    public enum class CatchUpBehavior {
        /**
         * Skip all missed tasks. The schedule state is updated to the current time, and no missed executions
         * are triggered.
         */
        Skip,

        /**
         * Execute exactly one missed task (the earliest one), then update the schedule state to the current time.
         * Any additional tasks that were missed beyond the first are dropped.
         */
        ExecuteOne,

        /**
         * Execute all missed tasks sequentially before resuming the normal schedule. Use with care if many
         * tasks could have been missed during downtime, as this may cause a burst of work upon startup.
         */
        ExecuteAll,
    }
}
