package com.copperleaf.ballast.queue.executor

import com.copperleaf.ballast.queue.JobCompletionResult
import com.copperleaf.ballast.queue.JobCompletionResultType
import com.copperleaf.ballast.queue.QueueDriver
import com.copperleaf.ballast.queue.QueueExecutor
import com.copperleaf.ballast.queue.QueueExecutorScope
import com.copperleaf.ballast.queue.SerializedJob
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
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
    private val adapter: QueueDriver.Adapter<JobMetadata, Payload, Result, State>,
    private val serializers: QueueExecutor.Serializers<Payload, Result, State>,
    private val captureErrorStacktrace: Boolean = false,
    private val timeSource: TimeSource = TimeSource.Monotonic,
) : QueueExecutor<JobMetadata, Payload, Result, State> {

// Run job queue Flow
// ---------------------------------------------------------------------------------------------------------------------

    override fun runQueue(
        queueName: String,
        processJob: suspend QueueExecutorScope<State>.(Payload) -> Result?
    ): Flow<JobProcessingResult<Result>> {
        return driver
            .observeQueue(queueName)
            .map { prepareJob(it) } // deserialize stored JSON to real object
            .map { runJob(it, processJob) } // run the job on a coroutine, respecting timeouts, cancellation, etc.
            .onEach { finalizeJob(it) } // convert result data back to JSON, then mark the job as completed or failed, or re-enqueue it for retry
    }

    private fun prepareJob(job: SerializedJob<JobMetadata>): RunningJob<JobMetadata, Payload, Result, State> {
        // extract JSON payloads, then deserialize to proper objects
        val payload = serializers.deserializePayload(job.serializedPayload)
        val state = serializers.deserializeState(job.serializedState)

        return RunningJob(
            jobId = job.jobId,
            payload = payload,
            state = state,
            attempts = job.attempts,
            metadata = job.metadata,
            timeoutDuration = job.timeoutDuration,
        )
    }

    private suspend fun runJob(
        job: RunningJob<JobMetadata, Payload, Result, State>,
        processJob: suspend QueueExecutorScope<State>.(Payload) -> Result?
    ): JobProcessingResult<Result> = coroutineScope {
        val mark = timeSource.markNow()

        // CompletableDeferred is used instead of a bare var so that concurrent writes from
        // inputProcessorJob and cancellationJob are safe: complete() is a no-op after the
        // first call, giving first-writer-wins semantics with no data race.
        val result = CompletableDeferred<JobProcessingResult<Result>>()

        val inputProcessorJob: Job = launch {
            try {
                // process the job with a timeout, respecting cancellation requests, and capturing intermediate state
                val scope = QueueExecutorScopeImpl(driver, serializers::serializeState, job.jobId, job.state)

                val processingResult = withTimeout(job.timeoutDuration) {
                    with(scope) {
                        processJob(job.payload)
                    }
                }

                result.complete(
                    JobProcessingResult(
                        jobId = job.jobId,
                        processingTime = mark.elapsedNow(),
                        result = JobCompletionResult.Success(processingResult),
                    )
                )
            } catch (e: TimeoutCancellationException) {
                // job was cancelled due to timeout
                result.complete(
                    JobProcessingResult(
                        jobId = job.jobId,
                        processingTime = mark.elapsedNow(),
                        result = JobCompletionResult.Timeout(
                            cause = e,
                            retryDelay = adapter.getDefaultRetryDelayTimeout(job.payload, job.attempts),
                        ),
                    )
                )
            } catch (e: JobFailureException) {
                // job failed with a known failure which is requesting a specific delay
                result.complete(
                    JobProcessingResult(
                        jobId = job.jobId,
                        processingTime = mark.elapsedNow(),
                        result = JobCompletionResult.Failure(
                            cause = (e.cause as? Exception?) ?: e,
                            retryDelay = e.retryDelay ?: adapter.getDefaultRetryDelayTimeout(job.payload, job.attempts),
                            permanentlyFail = e.permanentlyFail,
                            skipAttempt = e.skipAttempt,
                        ),
                    )
                )
            } catch (e: CancellationException) {
                // cooperate with coroutine cancellation from the downstream collector
                throw e
            } catch (e: Exception) {
                // job failed with an unknown exception
                result.complete(
                    JobProcessingResult(
                        jobId = job.jobId,
                        processingTime = mark.elapsedNow(),
                        result = JobCompletionResult.Failure(
                            cause = e,
                            retryDelay = adapter.getDefaultRetryDelayTimeout(job.payload, job.attempts),
                            permanentlyFail = false,
                            skipAttempt = false,
                        ),
                    )
                )
            }
        }

        val cancellationJob = launch {
            driver
                .subscribeToJobCancellation(job.jobId)
                .onEach {
                    // complete() is a no-op if inputProcessorJob already set a result, ensuring
                    // the first outcome (job completion or cancellation signal) always wins.
                    result.complete(
                        JobProcessingResult(
                            jobId = job.jobId,
                            processingTime = mark.elapsedNow(),
                            result = JobCompletionResult.Cancelled(
                                retryDelay = adapter.getDefaultRetryDelayTimeout(job.payload, job.attempts)
                            ),
                        )
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

        // Safety net: if neither coroutine completed the result (e.g. inputProcessorJob was
        // cancelled by an external scope rather than by the cancellationJob), treat as cancelled.
        result.complete(
            JobProcessingResult(
                jobId = job.jobId,
                processingTime = mark.elapsedNow(),
                result = JobCompletionResult.Cancelled(
                    retryDelay = adapter.getDefaultRetryDelayTimeout(job.payload, job.attempts)
                ),
            )
        )

        result.await()
    }

    private suspend fun finalizeJob(result: JobProcessingResult<Result>): Result? {
        when (result.result) {
            is JobCompletionResult.Success -> {
                driver.completeJobSuccessfully(
                    jobId = result.jobId,
                    processingTime = result.processingTime,
                    resultType = JobCompletionResultType.Success,
                    serializedResultData = if (result.result.resultData != null) {
                        // if the job completed with a result, serialize it and set it as the result
                        serializers.serializeResult(result.result.resultData)
                    } else {
                        null
                    },
                )
                return result.result.resultData
            }
            is JobCompletionResult.Cancelled -> {
                driver.completeJobWithFailure(
                    jobId = result.jobId,
                    processingTime = result.processingTime,
                    resultType = JobCompletionResultType.Cancelled,
                    retryDelay = result.result.retryDelay,
                    permanentlyFail = false,
                    skipAttempt = false,
                    failureMessage = null,
                    failureStacktrace = null,
                )
                return null
            }
            is JobCompletionResult.Timeout -> {
                driver.completeJobWithFailure(
                    jobId = result.jobId,
                    processingTime = result.processingTime,
                    resultType = JobCompletionResultType.Timeout,
                    retryDelay = result.result.retryDelay,
                    permanentlyFail = false,
                    skipAttempt = false,
                    failureMessage = result.result.cause.message,
                    failureStacktrace = null
                )
                return null
            }
            is JobCompletionResult.Failure -> {
                driver.completeJobWithFailure(
                    jobId = result.jobId,
                    processingTime = result.processingTime,
                    resultType = JobCompletionResultType.Failure,
                    retryDelay = result.result.retryDelay,
                    permanentlyFail = result.result.permanentlyFail,
                    skipAttempt = result.result.skipAttempt,
                    failureMessage = result.result.cause.message,
                    failureStacktrace = if (captureErrorStacktrace) {
                        result.result.cause.stackTraceToString()
                    } else {
                        null
                    },
                )
                return null
            }
        }
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
