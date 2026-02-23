package com.copperleaf.ballast.scheduler.alarmmanager

import com.copperleaf.ballast.scheduler.NamedSchedule
import com.copperleaf.ballast.scheduler.SchedulerCallback
import com.copperleaf.ballast.scheduler.executor.event.EventDrivenScheduleExecutor

@Suppress("UNCHECKED_CAST")
public class BallastAlarmManager<S : NamedSchedule, C : SchedulerCallback> private constructor(
    public val executor: EventDrivenScheduleExecutor<S, C>,
) {
    public companion object {
        private var instance: BallastAlarmManager<*, *>? = null

        public fun <S : NamedSchedule, C : SchedulerCallback> initialize(executor: EventDrivenScheduleExecutor<S, C>) {
            require(instance == null) { "BallastAlarmManager is already initialized" }
            instance = BallastAlarmManager(executor)
        }

        public fun getInstance(): BallastAlarmManager<*, *> {
            return requireNotNull(instance) { "BallastAlarmManager must be initialized" }
        }

        public fun <S : NamedSchedule, C : SchedulerCallback> getExecutor(): EventDrivenScheduleExecutor<S, C> {
            return (requireNotNull(instance) { "BallastAlarmManager must be initialized" } as BallastAlarmManager<S, C>)
                .executor
        }
    }
}
