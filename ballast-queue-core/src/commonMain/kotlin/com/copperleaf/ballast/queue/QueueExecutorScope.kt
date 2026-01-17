package com.copperleaf.ballast.queue

public interface QueueExecutorScope<State> {
    public suspend fun getCurrentState(): State
    public suspend fun setState(state: State)
}
