package com.copperleaf.ballast.scheduler.executor.poll

import com.copperleaf.ballast.scheduler.Schedule
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlin.time.Instant

public class InMemoryScheduleState(
    initialState: Map<String?, Instant> = emptyMap()
) : PollingScheduleExecutor.State {
    private val _lastExecutions = MutableStateFlow(initialState)
    public val lastExecutions: StateFlow<Map<String?, Instant>> get() = _lastExecutions.asStateFlow()

    override suspend fun getLastExecution(
        scheduleName: String?,
        schedule: Schedule,
    ): Instant? {
        return _lastExecutions.value[scheduleName]
    }

    override suspend fun storeExecution(
        scheduleName: String?,
        schedule: Schedule,
        instant: Instant
    ) {
        _lastExecutions.update {
            it + (scheduleName to instant)
        }
    }
}
