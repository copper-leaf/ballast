package com.copperleaf.ballast.queue.executor

import com.copperleaf.ballast.queue.QueueDriver
import com.copperleaf.ballast.queue.QueueExecutorScope

internal class QueueExecutorScopeImpl<JobMetadata : Any, State : Any>(
    private val driver: QueueDriver<JobMetadata>,
    private val stateSerializer: (State) -> String,
    private val jobId: String,
    initialState: State,
) : QueueExecutorScope<State> {
    private var currentState: State = initialState

    override suspend fun getCurrentState(): State {
        return currentState
    }

    override suspend fun setState(state: State) {
        driver.updateJobState(jobId, stateSerializer(state))
        currentState = state
    }
}
