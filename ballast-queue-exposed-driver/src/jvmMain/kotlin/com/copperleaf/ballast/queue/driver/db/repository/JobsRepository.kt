package com.copperleaf.ballast.queue.driver.db.repository

import com.copperleaf.ballast.queue.JobCompletionResultType
import com.copperleaf.ballast.queue.SerializedJob
import com.copperleaf.ballast.queue.driver.DatabaseQueueDriver
import kotlin.time.Duration
import kotlin.uuid.Uuid

public interface JobsRepository {

    public suspend fun getAllJobs(): List<SerializedJob<DatabaseQueueDriver.Metadata>>

    public suspend fun getAllJobsInQueue(
        queueName: String,
    ): List<SerializedJob<DatabaseQueueDriver.Metadata>>

    public suspend fun claimNextAvailableJob(
        queueName: String,
        leaseBufferDuration: Duration,
    ): SerializedJob<DatabaseQueueDriver.Metadata>?

    public suspend fun insertJob(
        queueName: String,
        serializedPayload: String,
        serializedInitialState: String,
        timeoutDuration: Duration,
        metadata: DatabaseQueueDriver.Metadata,
    ): Uuid

    public suspend fun completeJob(
        jobId: Uuid,
        processingTime: Duration,
        resultType: JobCompletionResultType,
        serializedResultData: String?,
    )

    public suspend fun retryOrPermanentlyFailJob(
        jobId: Uuid,
        processingTime: Duration,
        resultType: JobCompletionResultType,
        retryDelay: Duration,
        permanentlyFail: Boolean,
        failureMessage: String?,
        failureStacktrace: String?,
    )

    public suspend fun setJobState(
        jobId: Uuid,
        serializedState: String,
    )

    public suspend fun requestCancellation(
        jobId: Uuid,
    )

    public suspend fun isJobCancelled(
        jobId: Uuid,
    ): Boolean

    public suspend fun deleteJob(
        jobId: Uuid,
    )

    public suspend fun forceRetry(
        jobId: Uuid,
        retryDelay: Duration = Duration.ZERO,
        additionalAttempts: Int = 1,
    )
}
