package com.copperleaf.ballast.queue.driver.db

import com.copperleaf.ballast.queue.JobCompletionResultType
import com.copperleaf.ballast.queue.QueueDriver
import com.copperleaf.ballast.queue.QueueThrottle
import com.copperleaf.ballast.queue.SerializedJob
import com.copperleaf.ballast.queue.driver.db.repository.JobsRepository
import com.copperleaf.ballast.queue.pollingFlow
import com.copperleaf.ballast.queue.queueDriverPollingFlow
import com.copperleaf.ballast.queue.throttle.UnlimitedThrottle
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant
import kotlin.uuid.Uuid

public class ExposedDatabaseQueueDriver(
    private val repository: JobsRepository,
    private val throttle: QueueThrottle = UnlimitedThrottle(),
) : QueueDriver<ExposedDatabaseQueueDriver.Metadata> {

// Types
// ---------------------------------------------------------------------------------------------------------------------

    public data class Metadata(
        val insertedAt: Instant,
        val maxAttempts: Int = 5,
        val retryUntil: Instant? = null,

        val deduplicationKey: String? = null,
        val deduplicationDuration: Duration? = null,

        val messageGroup: String? = null,

        val priority: Int = 0,
        val runAt: Instant = insertedAt,
        val status: ExposedDatabaseJobStatus = ExposedDatabaseJobStatus.Pending,

        val leaseBufferDuration: Duration = 30.seconds,
        val leasedAt: Instant? = null,
        val leasedUntil: Instant? = null,

        val lastRunFinishedAt: Instant? = null,
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

// Insert/Query Operations
// ---------------------------------------------------------------------------------------------------------------------

    override suspend fun addToQueue(
        queueName: String,
        serializedPayload: String,
        serializedInitialState: String,
        timeoutDuration: Duration,
        metadata: Metadata,
    ): String {
        return repository
            .insertJob(
                queueName,
                serializedPayload,
                serializedInitialState,
                timeoutDuration,
                metadata,
            )
            .toString()
    }

    override fun observeQueue(queueName: String): Flow<SerializedJob<Metadata>> {
        return queueDriverPollingFlow(
            queueName = queueName,
            throttle = throttle,
            pollNext = { pollNext(queueName) },
            awaitNext = { delay(1.seconds) }
        )
    }

    internal suspend fun pollNext(
        queueName: String,
    ): SerializedJob<Metadata>? {
        return repository.claimNextAvailableJob(queueName)
    }

// Job Processing State/Results
// ---------------------------------------------------------------------------------------------------------------------

    override suspend fun updateJobState(jobId: String, serializedState: String) {
        repository.setJobState(
            jobId = Uuid.parse(jobId),
            serializedState = serializedState,
        )
    }

    override suspend fun completeJobSuccessfully(
        jobId: String,
        processingTime: Duration,
        resultType: JobCompletionResultType,
        serializedResultData: String?
    ) {
        repository.completeJob(
            jobId = Uuid.parse(jobId),
            processingTime = processingTime,
            resultType = resultType,
            serializedResultData = serializedResultData,
        )
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
        repository.retryOrPermanentlyFailJob(
            jobId = Uuid.parse(jobId),
            processingTime = processingTime,
            resultType = resultType,
            retryDelay = retryDelay,
            permanentlyFail = permanentlyFail,
            skipAttempt = skipAttempt,
            failureMessage = failureMessage ?: "Unknown error",
            failureStacktrace = failureStacktrace,
        )
    }

// Cancellation
// ---------------------------------------------------------------------------------------------------------------------

    override suspend fun requestJobCancellation(jobId: String) {
        repository.requestCancellation(
            jobId = Uuid.parse(jobId),
        )
    }

    override fun subscribeToJobCancellation(jobId: String): Flow<Unit> {
        return pollingFlow(
            pollNext = {
                if (repository.isJobCancelled(Uuid.parse(jobId))) {
                    Unit
                } else {
                    null
                }
            },
            awaitNext = { delay(1.seconds) }
        )
    }

// Utils
// ---------------------------------------------------------------------------------------------------------------------

}


/*

UPDATE jobs
SET
    status=
        CASE WHEN (jobs.unique_until IS NOT NULL) AND (jobs.unique_until > CURRENT_TIMESTAMP)
        THEN
            CAST('Cooldown' AS job_status) ELSE CAST('Succeeded' AS job_status)
        END,
    result_data=$1::jsonb,
    last_run_result_type=$2,
    last_run_duration=$3,
    last_run_finished_at=$4,
    last_run_failure_message=$5,
    last_run_failure_stacktrace=$6
WHERE jobs.id = $7


 */
