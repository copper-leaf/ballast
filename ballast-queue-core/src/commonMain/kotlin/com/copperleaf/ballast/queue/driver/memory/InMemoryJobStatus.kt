package com.copperleaf.ballast.queue.driver.memory

import com.copperleaf.ballast.queue.QueueDriver

public enum class InMemoryJobStatus {

    /**
     * The job is inserted into the queue and is waiting to be processed. If a job failed during processed but is
     * eligible to be retried, it will be moved back to the `Pending` state.
     */
    Pending,

    /**
     * The job has been selected for processing and is currently being worked on.
     *
     * It is possible for a job to be left in the `Running` state indefinitely if the worker processing it crashes or
     * is terminated externally. Therefore, the [QueueDriver] must implement a way to detect and recover such jobs, by
     * moving them back to the `Pending` or `Failed` state according to its retry policy.
     */
    Running,

    /**
     * The job has finished processing successfully.
     */
    Completed,

    /**
     * The job has failed during processing, and should be considered a permanent failure. It is not eligible for
     * automatic retry, though it may be manually retried or inspected later.
     */
    Failed,
}
