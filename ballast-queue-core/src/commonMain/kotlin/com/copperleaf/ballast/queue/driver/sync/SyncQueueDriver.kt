package com.copperleaf.ballast.queue.driver.sync

import com.copperleaf.ballast.queue.JobCompletionResultType
import com.copperleaf.ballast.queue.QueueDriver
import com.copperleaf.ballast.queue.SerializedJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlin.time.Duration
import kotlin.uuid.Uuid

/**
 * The Sync Queue Driver is a implementation of a [com.copperleaf.ballast.queue.QueueDriver] that is intended for unit testing. It does not
 * actually kep a queue of jobs, but instead uses a [kotlinx.coroutines.channels.Channel.Factory.RENDEZVOUS] Channel to immediately process the job synchronously.
 * This allows you to have guarantees in your unit tests that calling [addToQueue] will process the job before
 * returning, as long as another coroutine is currently observing the queue.
 *
 * In general, this driver assumes that the job will complete successfully. It does not support tracking metadata about
 * the job, so it cannot be queried for job status or results.
 *
 * This queue does not support the typical features of a persistent queue, such as retries, timeouts, or job state
 * updates. It is only intended for unit tests where you need prompt guarantees of the job being processed in an
 * end-to-end scenario. One example would be testing that an endpoint to Create a resource, then a background job posts
 * the created ID to a separate fine-grained authorization service. A follow-up endpoint needs to be called to verify
 * the permissions were created correctly, and that the resource is accessible. if the queue were asynchronous, you
 * would need to introduce arbitrary delays or polling to verify the end state, which would make your tests slower and
 * flaky.
 */
public class SyncQueueDriver() : QueueDriver<Unit> {

    private val channel = Channel<SerializedJob<Unit>>(Channel.Factory.RENDEZVOUS)

    private var _lastJob: SerializedJob<Unit>? = null
    private var _lastJobResultType: JobCompletionResultType? = null
    private var _lastJobResultData: String? = null
    private var _lastJobFailureMessage: String? = null

    public val lastJob: SerializedJob<Unit>? get() = _lastJob
    public val lastJobResultType: JobCompletionResultType? get() = _lastJobResultType
    public val lastJobResultData: String? get() = _lastJobResultData
    public val lastJobFailureMessage: String? get() = _lastJobFailureMessage

// Insert/Query Operations
// ---------------------------------------------------------------------------------------------------------------------

    override suspend fun addToQueue(
        queueName: String,
        serializedPayload: String,
        serializedInitialState: String,
        timeoutDuration: Duration,
        metadata: Unit,
    ): String {
        val serializedJob = SerializedJob(
            jobId = Uuid.Companion.random().toString(),
            queueName = queueName,
            timeoutDuration = timeoutDuration,
            serializedPayload = serializedPayload,
            serializedState = serializedInitialState,
            serializedResultData = null,
            attempts = 1,
            metadata = metadata,
        )

        channel.send(serializedJob)

        return serializedJob.jobId
    }

    override fun observeQueue(
        queueName: String,
    ): Flow<SerializedJob<Unit>> {
        return channel
            .receiveAsFlow()
            .onEach { job ->
                _lastJob = job
                _lastJobResultType = null
                _lastJobResultData = null
                _lastJobFailureMessage = null
            }
    }

// Job Processing State/Results
// ---------------------------------------------------------------------------------------------------------------------

    override suspend fun updateJobState(
        jobId: String,
        serializedState: String,
    ) {
        // no-op
    }

    override suspend fun completeJobSuccessfully(
        jobId: String,
        processingTime: Duration,
        resultType: JobCompletionResultType,
        serializedResultData: String?
    ) {
        _lastJobResultType = resultType
        _lastJobResultData = serializedResultData
        _lastJobFailureMessage = null
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
        _lastJobResultType = resultType
        _lastJobResultData = null
        _lastJobFailureMessage = failureMessage
    }

    // Cancellation
// ---------------------------------------------------------------------------------------------------------------------

    override suspend fun requestJobCancellation(jobId: String) {
        throw NotImplementedError("Cancellation not supported")
    }

    override fun subscribeToJobCancellation(jobId: String): Flow<Unit> {
        return emptyFlow()
    }
}
