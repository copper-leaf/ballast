package com.copperleaf.ballast.queue

import kotlinx.coroutines.flow.Flow
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

/**
 * A QueueExecutor is a higher-level of abstraction over a [QueueDriver], allowing you to use typed objects as your
 * Jobs, which then get serialized/deserialized automatically as they are inserted into and pulled from the queue.
 */
public interface QueueExecutor<
        JobMetadata : Any,
        Payload : Any,
        Result : Any,
        State : Any,
        > {

    public fun runQueue(
        queueName: String,
        processJob: suspend QueueExecutorScope<State>.(Payload) -> Result?
    ): Flow<Unit>

    public suspend fun insertJob(
        queueName: String,
        payload: Payload,
        initialState: State,
    ): String

    public interface Adapter<
            JobMetadata : Any,
            Payload : Any,
            Result : Any,
            State : Any,
            > {
        public fun getJobTimeout(payload: Payload): Duration {
            return 30.seconds
        }

        public fun getDefaultRetryDelayTimeout(payload: Payload): Duration {
            return 1.minutes
        }

        public fun getJobMetadata(payload: Payload): JobMetadata
    }

    public interface Serializers<
            Payload : Any,
            Result : Any,
            State : Any,
            > {
        public fun serializePayload(payload: Payload): String
        public fun deserializePayload(serializedPayload: String): Payload

        public fun serializeResult(result: Result): String
        public fun deserializeResult(serializedResult: String): Result

        public fun serializeState(state: State): String
        public fun deserializeState(serializedState: String): State
    }
}
