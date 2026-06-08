package com.copperleaf.ballast.scheduler.alarmmanager

import com.copperleaf.ballast.scheduler.NamedSchedule
import com.copperleaf.ballast.scheduler.SchedulerCallback
import com.copperleaf.ballast.scheduler.executor.event.EventDrivenScheduleExecutor

@Suppress("UNCHECKED_CAST")
public class BallastAlarmManager<S : NamedSchedule, C : SchedulerCallback> private constructor(
    public val executor: EventDrivenScheduleExecutor<S, C>,
    public val precision: AlarmPrecision,
) {
    public companion object {
        private var configurations: MutableMap<String?, BallastAlarmManager<*, *>> = mutableMapOf()

        public fun <S : NamedSchedule, C : SchedulerCallback> initialize(
            executor: EventDrivenScheduleExecutor<S, C>,
            precision: AlarmPrecision = AlarmPrecision.Default,
        ): EventDrivenScheduleExecutor<S, C> {
            require(configurations[null] == null) { "BallastAlarmManager default configuration is already initialized" }
            configurations[null] = BallastAlarmManager(
                executor = executor,
                precision = precision,
            )
            return executor
        }

        public fun <S : NamedSchedule, C : SchedulerCallback> initialize(
            configurationName: String,
            executor: EventDrivenScheduleExecutor<S, C>,
            precision: AlarmPrecision = AlarmPrecision.Default,
        ): EventDrivenScheduleExecutor<S, C> {
            require(configurations[configurationName] == null) { "BallastAlarmManager configuration '$configurationName' is already initialized" }
            configurations[configurationName] = BallastAlarmManager(
                executor = executor,
                precision = precision,
            )
            return executor
        }

        public fun getInstance(): BallastAlarmManager<*, *> {
            return requireNotNull(configurations[null]) { "BallastAlarmManager default configuration must be initialized" }
        }

        public fun getInstance(configurationName: String?): BallastAlarmManager<*, *> {
            return requireNotNull(configurations[configurationName]) { "BallastAlarmManager configuration '$configurationName' must be initialized" }
        }

        public fun getAllConfigurations(): List<BallastAlarmManager<*, *>> {
            return configurations.values.toList()
        }
    }
}
