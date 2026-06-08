@file:Suppress("UNCHECKED_CAST")

package com.copperleaf.ballast.queue

public enum class JobCompletionResultType {
    /**
     * The job completed successfully. Store the result payload for later use, if needed. This job is a candidate for
     * deletion from the queue.
     */
    Success,

    /**
     * The job was cancelled before processing completed. This job is a candidate for being retried according to the
     * queue's retry policy.
     */
    Cancelled,

    /**
     * The job failed because it was processing for too long and was cancelled due to a timeout. This job is a candidate
     * for being retried according to the queue's retry policy.
     */
    Timeout,

    /**
     * The job failed abnormally due to an Exception thrown during processing. This job is a candidate for being retried
     * according to the queue's retry policy.
     */
    Failure,
}
