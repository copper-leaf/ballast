package com.copperleaf.ballast.scheduler.executor

import com.copperleaf.ballast.scheduler.NamedSchedule
import com.copperleaf.ballast.scheduler.ScheduleExecutor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlin.time.Instant

public class InMemoryScheduleState : ScheduleExecutor.State {
    private val _lastExecutions = MutableStateFlow<Map<String, Instant>>(emptyMap())
    public val lastExecutions: StateFlow<Map<String, Instant>> get() = _lastExecutions.asStateFlow()

    override suspend fun getLastExecution(schedule: NamedSchedule): Instant? {
        return _lastExecutions.value[schedule.name]
    }

    override suspend fun storeExecution(
        schedule: NamedSchedule,
        instant: Instant
    ) {
        _lastExecutions.update {
            it + (schedule.name to instant)
        }
    }
}
