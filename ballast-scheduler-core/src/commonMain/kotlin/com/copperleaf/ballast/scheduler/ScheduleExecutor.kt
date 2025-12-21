package com.copperleaf.ballast.scheduler

import kotlinx.coroutines.flow.Flow
import kotlin.time.Instant


public interface ScheduleExecutor {
    /**
     * Executes a single [NamedSchedule], producing a [Flow] of [ScheduleEmission] events indicating tasks to be
     * completed. Tasks should be fully handled directly in the flow if you wish to apply backpressure to the schedule
     * emissions, dropping emissions from the upstream schedule that would have been emitted while the previous task
     * was still being handled.
     */
    public fun runSchedule(schedule: NamedSchedule): Flow<ScheduleEmission>

    /**
     * Executes multiple [NamedSchedule]s, producing a [Flow] of [ScheduleEmission] events indicating tasks to be
     * completed. Tasks from all schedules with be merged into one with the [kotlinx.coroutines.flow.merge] operator,
     * which does not allow backpressure to be applied to the individual schedule's original upstream flow (since
     * backpressure would block all schedules, not just the slow one). Therefore, it is best to use this executor to
     * dispatch the scheduled tasks to another system that can handle backpressure, such as Ballast Queue.
     */
    public fun runSchedules(schedules: List<NamedSchedule>): Flow<ScheduleEmission>

    public interface State {
        public suspend fun getLastExecution(
            schedule: NamedSchedule,
        ): Instant?

        public suspend fun storeExecution(
            schedule: NamedSchedule,
            instant: Instant,
        )
    }
}
