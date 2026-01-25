@file:Suppress("UNCHECKED_CAST")

package com.copperleaf.ballast.queue

import kotlin.time.Duration

public data class SerializedJob<JobMetadata : Any>(
    /**
     * A unique ID identifying this job in the queue. If the job fails an is retried, it must retain the same ID.
     */
    val jobId: String,

    /**
     * The name of the queue this job belongs to. A "queue" is a logical grouping of jobs that a specific worker is
     * responsible to processing.
     */
    val queueName: String,

    /**
     * The payload of the job inserted into the queue. This property is immutable, and must not change between retries.
     */
    val serializedPayload: String,

    /**
     * The maximum duration the job is allowed to run before it gets terminated. A timeout indicates a failure of the
     * job, and it may be retried according to the queue's retry policy.
     */
    val timeoutDuration: Duration,

    /**
     * The state of the job may be updated during processing, and must be retained between retries. This property allows
     * jobs to report progress to an observer, or maintain intermediate state between attempts so the job can be resumed
     * from the middle rather than starting over from the beginning.
     *
     * For example, a job may batch-upload a large number of files to a remote server. The [serializedPayload] contains the
     * list of files to be uploaded, and the [serializedState] contains the list of files that have been successfully
     * uploaded. An observer can display the process percentage by comparing the two lists. And if the job fails or
     * times out, when it is retried later, it will only need to upload the remaining files, not all files in the
     * initial payload.
     *
     * This property is entirely controlled by the job processor; the queue driver must not interpret or modify its
     * contents.
     */
    val serializedState: String,

    /**
     * The result of the job after processing the latest attempt. It typically will contain information tracked by the
     * QueueExecutor about the outcome of the processing attempt, such as an error message, stacktrace, or result data.
     */
    val serializedResultData: String?,

    /**
     * The number of times this job has been attempted. This starts at 0, and in incremented by 1 each time it is run.
     * The very first time a job is attempted, this will be 1. If it fails ad is retried, the first retry is 2, etc.
     */
    val attempts: Int = 0,

    /**
     * Arbitrary data about this job that the [QueueDriver] uses to manage the job in the queue and implement its own
     * queuing policies. This data is expected to be irrelevant to the processing of the job itself, but may be needed
     * to determine how and when to process or retry the job.
     */
    val metadata: JobMetadata,
)
