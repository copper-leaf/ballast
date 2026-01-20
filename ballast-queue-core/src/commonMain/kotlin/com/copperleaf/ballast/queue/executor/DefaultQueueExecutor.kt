package com.copperleaf.ballast.queue.executor

import com.copperleaf.ballast.queue.JobCompletionResult
import com.copperleaf.ballast.queue.JobCompletionResultType
import com.copperleaf.ballast.queue.QueueDriver
import com.copperleaf.ballast.queue.QueueExecutor
import com.copperleaf.ballast.queue.QueueExecutorScope
import com.copperleaf.ballast.queue.SerializedJob
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import kotlin.time.TimeSource

@OptIn(ExperimentalCoroutinesApi::class)
public class DefaultQueueExecutor<
        JobMetadata : Any,
        Payload : Any,
        Result : Any,
        State : Any,
        >(
    private val driver: QueueDriver<JobMetadata>,
    private val adapter: QueueExecutor.Adapter<JobMetadata, Payload, Result, State>,
    private val serializers: QueueExecutor.Serializers<Payload, Result, State>,
    private val captureErrorStacktrace: Boolean = false,
    private val timeSource: TimeSource = TimeSource.Monotonic,
) : QueueExecutor<JobMetadata, Payload, Result, State> {

// Run job queue Flow
// ---------------------------------------------------------------------------------------------------------------------

    override fun runQueue(
        queueName: String,
        processJob: suspend QueueExecutorScope<State>.(Payload) -> Result?
    ): Flow<Unit> {
        return driver
            .observeQueue(queueName)
            .map { prepareJob(it) } // deserialize stored JSON to real object
            .map { runJob(it, processJob) } // run the job on a coroutine, respecting timeouts, cancellation, etc.
            .map { finalizeJob(it) } // convert result data back to JSON, then mark the job as completed or failed, or re-enqueue it for retry
    }

    private fun prepareJob(job: SerializedJob<JobMetadata>): RunningJob<Payload, Result, State> {
        // extract JSON payloads, then deserialize to proper objects
        val payload = serializers.deserializePayload(job.serializedPayload)
        val state = serializers.deserializeState(job.serializedState)

        return RunningJob(
            jobId = job.jobId,
            payload = payload,
            state = state,
            timeoutDuration = job.timeoutDuration,
        )
    }

    private suspend fun runJob(
        job: RunningJob<Payload, Result, State>,
        processJob: suspend QueueExecutorScope<State>.(Payload) -> Result?
    ): JobProcessingResult<Result> = coroutineScope {
        val mark = timeSource.markNow()
        var result: JobProcessingResult<Result>? = null

        val inputProcessorJob: Job = launch {
            try {
                // process the job with a timeout, respecting cancellation requests, and capturing intermediate state
                val scope = QueueExecutorScopeImpl(driver, serializers::serializeState, job.jobId, job.state)

                val processingResult = withTimeout(job.timeoutDuration) {
                    with(scope) {
                        processJob(job.payload)
                    }
                }

                result = JobProcessingResult(
                    jobId = job.jobId,
                    processingTime = mark.elapsedNow(),
                    result = JobCompletionResult.Success(processingResult),
                )
            } catch (e: TimeoutCancellationException) {
                // job was cancelled due to timeout
                result = JobProcessingResult(
                    jobId = job.jobId,
                    processingTime = mark.elapsedNow(),
                    result = JobCompletionResult.Timeout(e, adapter.getDefaultRetryDelayTimeout(job.payload)),
                )
            } catch (e: JobFailureException) {
                // job failed with a known failure which is requesting a specific delay
                result = JobProcessingResult(
                    jobId = job.jobId,
                    processingTime = mark.elapsedNow(),
                    result = JobCompletionResult.Failure(e.cause as Exception, e.retryDelay),
                )
            } catch (e: CancellationException) {
                // cooperate with coroutine cancellation from the downstream collector
                throw e
            } catch (e: Exception) {
                // job failed with an unknown exception
                result = JobProcessingResult(
                    jobId = job.jobId,
                    processingTime = mark.elapsedNow(),
                    result = JobCompletionResult.Failure(e, adapter.getDefaultRetryDelayTimeout(job.payload)),
                )
            }
        }

        val cancellationJob = launch {
            driver
                .subscribeToJobCancellation(job.jobId)
                .onEach {
                    result = JobProcessingResult(
                        jobId = job.jobId,
                        processingTime = mark.elapsedNow(),
                        result = JobCompletionResult.Cancelled(adapter.getDefaultRetryDelayTimeout(job.payload)),
                    )
                    inputProcessorJob.cancel()
                    inputProcessorJob.join()
                }
                .launchIn(this)
        }

        inputProcessorJob.join()

        // once the inputProcessorJob has completed, cancel the cancellationJob so we can exit this function
        cancellationJob.cancel()
        cancellationJob.join()

        result!!
    }

    private suspend fun finalizeJob(result: JobProcessingResult<Result>) {
        // mark the job as completed, with either success or failure
        driver.markJobCompleted(
            jobId = result.jobId,
            processingTime = result.processingTime,
            resultType = when (result.result) {
                is JobCompletionResult.Success -> JobCompletionResultType.Success
                is JobCompletionResult.Cancelled -> JobCompletionResultType.Cancelled
                is JobCompletionResult.Timeout -> JobCompletionResultType.Timeout
                is JobCompletionResult.Failure -> JobCompletionResultType.Failure
            },
            serializedResultData = when (result.result) {
                is JobCompletionResult.Success -> if (result.result.resultData != null) {
                    // if the job completed with a result, serialize it and set it as the result
                    serializers.serializeResult(result.result.resultData)
                } else {
                    null
                }

                is JobCompletionResult.Cancelled -> null
                is JobCompletionResult.Timeout -> null
                is JobCompletionResult.Failure -> null
            },
            retryDelay = when (result.result) {
                is JobCompletionResult.Success -> null
                is JobCompletionResult.Cancelled -> result.result.retryDelay
                is JobCompletionResult.Timeout -> result.result.retryDelay
                is JobCompletionResult.Failure -> result.result.retryDelay
            },
            failureMessage = when (result.result) {
                is JobCompletionResult.Success -> null
                is JobCompletionResult.Cancelled -> null
                is JobCompletionResult.Timeout -> result.result.cause.message
                is JobCompletionResult.Failure -> result.result.cause.message
            },
            failureStacktrace = when (result.result) {
                is JobCompletionResult.Success -> null
                is JobCompletionResult.Cancelled -> null
                is JobCompletionResult.Timeout -> null

                is JobCompletionResult.Failure -> if (captureErrorStacktrace) {
                    result.result.cause.stackTraceToString()
                } else {
                    null
                }
            },
        )
    }

// Serialize and enqueue a job
// ---------------------------------------------------------------------------------------------------------------------

    override suspend fun insertJob(
        queueName: String,
        payload: Payload,
        initialState: State,
    ): String {
        val serializedPayload = serializers.serializePayload(payload)
        val serializedState = serializers.serializeState(initialState)
        val timeout = adapter.getJobTimeout(payload)
        val metadata = adapter.getJobMetadata(payload)

        return driver.addToQueue(
            queueName = queueName,
            serializedPayload = serializedPayload,
            serializedInitialState = serializedState,
            timeoutDuration = timeout,
            metadata = metadata,
        )
    }
}
