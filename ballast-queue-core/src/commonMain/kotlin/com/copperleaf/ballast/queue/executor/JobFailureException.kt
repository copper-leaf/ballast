package com.copperleaf.ballast.queue.executor

import kotlin.time.Duration

public class JobFailureException(
    cause: Exception?,

    /**
     * If set, indicates that the job should be retried after this delay period if it has any attempts left. If null,
     * the retry delay will be set by [com.copperleaf.ballast.queue.QueueExecutor.Adapter.getDefaultRetryDelayTimeout].
     */
    public val retryDelay: Duration?,

    /**
     * If true, indicates that the job should be marked as permanently failed immediately, without any further retries.
     * Useful for scenarios where the runtime can detect that the job will never succeed so retries will only waste
     * compute resources, such as invalid input data or environmental changes that render the job obsolete.
     */
    public val permanentlyFail: Boolean = false,
) : RuntimeException(cause)
