package com.copperleaf.ballast.queue.executor

import kotlin.time.Duration

public class JobFailureException(
    cause: Exception?,

    /**
     * If set, indicates that the job should be retried after this delay period if it has any attempts left. If null,
     * the retry delay will be set by [com.copperleaf.ballast.queue.QueueDriver.Adapter.getDefaultRetryDelayTimeout].
     */
    public val retryDelay: Duration?,

    /**
     * If true, indicates that the job should be marked as permanently failed immediately, without any further retries.
     * Useful for scenarios where the runtime can detect that the job will never succeed so retries will only waste
     * compute resources, such as invalid input data or environmental changes that render the job obsolete.
     */
    public val permanentlyFail: Boolean = false,

    /**
     * If true, indicates that the job should be considered skipped without consuming one of its retry attempts. In
     * practice, it is up to the Driver to decide how to handle skipped jobs, but a common approach is to enqueue the
     * job for retry just as if it failed, but granting one additional retry attempt so that the job's total number of
     * retries is not reduced.
     *
     * This is useful for scenarios where the job cannot be processed at this time for some condition that cannot be
     * known ahead of time, but can be detected at runtime. For example, a job that depends on an external resource
     * that is currently unavailable, or a job requiring internet connectivity in mobile sync queues. By skipping the
     * job, it can be retried later without penalizing the job's retry count.
     */
    public val skipAttempt: Boolean = false,
) : RuntimeException(cause)
