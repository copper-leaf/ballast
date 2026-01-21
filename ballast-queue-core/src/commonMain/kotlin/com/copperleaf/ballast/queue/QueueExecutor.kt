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

        /**
         * Get the timeout duration for the given job payload. This is how long the job has to complete before it is
         * forcibly cancelled and marked as a failure. It may be retried according to the driver's retry policy.
         *
         * This is called when inserting a new job into the queue.
         */
        public fun getJobTimeout(payload: Payload): Duration {
            return 30.seconds
        }

        /**
         * Convert the payload into job metadata to be stored alongside the job in the queue. The metadata is not used
         * by the [QueueExecutor] itself, but is needed [QueueDriver] implementation to determine how and when to
         * enqueue and dequeue the job. Common data the Driver might store in the Metadata includes things like:
         *
         * - Initial delay
         * - Number of times the job has already run
         * - Max number of retry attempts before marking the job as permanently failed
         * - Timestamps for when the job was inserted, last attempted, next available run time, etc.
         *
         * This is called when inserting a new job into the queue.
         */
        public fun getJobMetadata(payload: Payload): JobMetadata

        /**
         * Called after a job failed and is being retried, to determine how long to wait before making the job
         * available to run again. The default implementation returns 1 minute. The [metadata] can be used to apply
         * custom retry backoff strategies based on the number of attempts or other data stored by the [QueueDriver].
         *
         * Jobs may instead throw [com.copperleaf.ballast.queue.executor.JobFailureException] during processing to
         * request a specific delay that was determined at runtime, rather than using this default value. That would be
         * common in scenarios such as network rate-limiting, where the server response indicates how long to wait.
         */
        public fun getDefaultRetryDelayTimeout(payload: Payload, metadata: JobMetadata): Duration {
            return 1.minutes
        }
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
