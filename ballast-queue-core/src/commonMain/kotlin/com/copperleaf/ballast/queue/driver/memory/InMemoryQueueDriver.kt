package com.copperleaf.ballast.queue.driver.memory

import com.copperleaf.ballast.queue.JobCompletionResultType
import com.copperleaf.ballast.queue.QueueDriver
import com.copperleaf.ballast.queue.QueueThrottle
import com.copperleaf.ballast.queue.SerializedJob
import com.copperleaf.ballast.queue.queueDriverPollingFlow
import com.copperleaf.ballast.queue.throttle.UnlimitedThrottle
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant
import kotlin.uuid.Uuid

/**
 * The In-memory Queue Driver is a simple implementation of a [QueueDriver] that keeps all jobs in a list in memory,
 * held in a [StateFlow] for observing the state of the queue and its jobs. This is primarily useful for testing and
 * debugging, as its jobs are NOT persisted between application restarts.
 *
 * It fundamentally operates like a real queue, but with limited flexibility in scheduling, and no persistence.
 *
 * Supported features:
 *
 * - **Multiple queues**: separated by name
 * - **Job prioritization**: The queue will always select the job with the highest priority to run next, delaying the
 *   execution of jobs with lower priority (even if they have an earlier start time).
 * - **Scheduling**: Jobs can be delayed to run at a specific time in the future
 * - **Retries**: Jobs that are cancelled or failed during processing will be scheduled for retry. The delay to wait
 *   between retries, and the number of times to retry a job, are configured per-job.
 * - **Cancellation**: Jobs can be cancelled while running, and will be rescheduled for retry if they have remaining
 *   attempts.
 */
public class InMemoryQueueDriver(
    private val clock: Clock = Clock.System,
    private val throttle: QueueThrottle = UnlimitedThrottle(),
) : QueueDriver<InMemoryQueueDriver.Metadata> {

// Types
// ---------------------------------------------------------------------------------------------------------------------

    public data class Metadata(
        val insertedAt: Instant,
        val maxAttempts: Int,

        val priority: Int = 0,
        val runAt: Instant = insertedAt,
        val status: InMemoryJobStatus = InMemoryJobStatus.Pending,

        val lastRunDuration: Duration? = null,
        val lastResultType: JobCompletionResultType? = null,
        val lastErrorMessage: String? = null,
        val lastStacktrace: String? = null,
    )

    public class DefaultAdapter<
            Payload : Any,
            Result : Any,
            State : Any,
            >(
        private val clock: Clock = Clock.System,
    ) : QueueDriver.Adapter<Metadata, Payload, Result, State> {
        override fun getJobMetadata(payload: Payload): Metadata {
            val now = clock.now()
            return Metadata(
                insertedAt = now,
                maxAttempts = 5,
            )
        }
    }

// Driver state
// ---------------------------------------------------------------------------------------------------------------------

    private val mutex = Mutex()
    private val queue = MutableStateFlow(emptyList<SerializedJob<Metadata>>())
    private val cancellations = MutableSharedFlow<String>()

// Insert/Query Operations
// ---------------------------------------------------------------------------------------------------------------------

    override suspend fun addToQueue(
        queueName: String,
        serializedPayload: String,
        serializedInitialState: String,
        timeoutDuration: Duration,
        metadata: Metadata,
    ): String {
        return mutex.withLock {
            val serializedJob = SerializedJob(
                jobId = Uuid.random().toString(),
                queueName = queueName,
                timeoutDuration = timeoutDuration,
                serializedPayload = serializedPayload,
                serializedState = serializedInitialState,
                serializedResultData = null,
                metadata = metadata,
            )
            queue.update { it + serializedJob }
            serializedJob.jobId
        }
    }

    override fun observeQueue(
        queueName: String,
    ): Flow<SerializedJob<Metadata>> {
        return queueDriverPollingFlow(
            queueName = queueName,
            throttle = throttle,
            pollNext = { pollNext(queueName) },
            awaitNext = { delay(1.seconds) },
        )
    }

    public fun observeQueueState(): StateFlow<List<SerializedJob<Metadata>>> {
        return queue.asStateFlow()
    }

    public fun observeJobState(jobId: String): Flow<SerializedJob<Metadata>> {
        return queue.mapNotNull {
            it.singleOrNull { job -> job.jobId == jobId }
        }
    }

    internal suspend fun pollNext(
        queueName: String,
    ): SerializedJob<Metadata>? {
        return mutex.withLock {
            val now = clock.now()

            val item = queue
                .value
                .asSequence()
                .filter { it.queueName == queueName }
                .filter { isReady(it, now) }
                .sortedBy { it.metadata.insertedAt } // oldest first so equal-priority jobs are FIFO
                .maxByOrNull { it.metadata.priority }

            if (item != null) {
                updateJobNoLock(item.jobId) {
                    it.copy(
                        attempts = it.attempts + 1,
                        metadata = it.metadata.copy(
                            status = InMemoryJobStatus.Running,
                        )
                    )
                }
            } else {
                null
            }
        }
    }

    private fun isReady(item: SerializedJob<Metadata>, now: Instant): Boolean {
        return item.metadata.status == InMemoryJobStatus.Pending &&
                item.metadata.runAt <= now
    }

// Job Processing State/Results
// ---------------------------------------------------------------------------------------------------------------------

    override suspend fun updateJobState(
        jobId: String,
        serializedState: String,
    ) {
        updateJob(jobId) {
            it.copy(serializedState = serializedState)
        }
    }

    override suspend fun completeJobSuccessfully(
        jobId: String,
        processingTime: Duration,
        resultType: JobCompletionResultType,
        serializedResultData: String?
    ) {
        updateJob(jobId) {
            it.copy(
                serializedResultData = serializedResultData,
                metadata = it.metadata.copy(
                    status = InMemoryJobStatus.Completed,
                    runAt = it.metadata.runAt,
                    lastRunDuration = processingTime,
                    lastResultType = resultType,
                    lastErrorMessage = null,
                    lastStacktrace = null,
                )
            )
        }
    }

    override suspend fun completeJobWithFailure(
        jobId: String,
        processingTime: Duration,
        resultType: JobCompletionResultType,
        retryDelay: Duration,
        permanentlyFail: Boolean,
        skipAttempt: Boolean,
        failureMessage: String?,
        failureStacktrace: String?
    ) {
        updateJob(jobId) {
            val shouldRetry = it.attempts < it.metadata.maxAttempts && !permanentlyFail

            it.copy(
                serializedResultData = null,
                metadata = it.metadata.copy(
                    status = when (resultType) {
                        JobCompletionResultType.Success -> {
                            error("Cannot complete job with failure using Success result type")
                        }

                        JobCompletionResultType.Cancelled,
                        JobCompletionResultType.Timeout,
                        JobCompletionResultType.Failure ->
                            if (shouldRetry) InMemoryJobStatus.Pending else InMemoryJobStatus.Failed
                    },
                    runAt = if (shouldRetry) clock.now() + retryDelay else it.metadata.runAt,
                    maxAttempts = if (skipAttempt) it.metadata.maxAttempts + 1 else it.metadata.maxAttempts,
                    lastRunDuration = processingTime,
                    lastResultType = resultType,
                    lastErrorMessage = failureMessage,
                    lastStacktrace = failureStacktrace,
                )
            )
        }
    }

// Shutdown
// ---------------------------------------------------------------------------------------------------------------------

    override suspend fun awaitShutdown() {
        throttle.awaitShutdown()
    }

    // Cancellation
// ---------------------------------------------------------------------------------------------------------------------

    override suspend fun requestJobCancellation(jobId: String) {
        cancellations.emit(jobId)
    }

    override fun subscribeToJobCancellation(jobId: String): Flow<Unit> {
        return cancellations.filter { it == jobId }.map { }
    }

// Utils
// ---------------------------------------------------------------------------------------------------------------------

    private suspend fun updateJob(
        jobId: String,
        transform: (SerializedJob<Metadata>) -> SerializedJob<Metadata>,
    ): SerializedJob<Metadata> {
        return mutex.withLock {
            updateJobNoLock(jobId, transform)
        }
    }

    private fun updateJobNoLock(
        jobId: String,
        transform: (SerializedJob<Metadata>) -> SerializedJob<Metadata>,
    ): SerializedJob<Metadata> {
        val queueList = queue.value.toMutableList()
        val index = queueList.indexOfFirst { it.jobId == jobId }
        queueList[index] = transform(queueList[index])
        queue.value = queueList.toList()
        return queue.value[index]
    }
}
