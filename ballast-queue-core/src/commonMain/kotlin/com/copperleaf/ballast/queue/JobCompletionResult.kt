@file:Suppress("UNCHECKED_CAST")

package com.copperleaf.ballast.queue

import kotlinx.coroutines.TimeoutCancellationException
import kotlin.time.Duration

public sealed interface JobCompletionResult<Result : Any> {
    /**
     * The job completed successfully. Store the result payload for later use, if needed. This job is a candidate for
     * deletion from the queue.
     */
    public data class Success<Result : Any>(val resultData: Result?) : JobCompletionResult<Result>

    /**
     * The job was cancelled before processing completed. This job is a candidate for being retried according to the
     * queue's retry policy.
     */
    public data class Cancelled<Result : Any>(val retryDelay: Duration) : JobCompletionResult<Result>

    /**
     * The job failed because it was processing for too long and was cancelled due to a timeout. This job is a candidate
     * for being retried according to the queue's retry policy.
     */
    public data class Timeout<Result : Any>(val cause: TimeoutCancellationException, val retryDelay: Duration) : JobCompletionResult<Result>

    /**
     * The job failed abnormally due to an Exception thrown during processing. This job is a candidate for being retried
     * according to the queue's retry policy.
     */
    public data class Failure<Result : Any>(val cause: Exception, val retryDelay: Duration, val permanentlyFail: Boolean) : JobCompletionResult<Result>
}
