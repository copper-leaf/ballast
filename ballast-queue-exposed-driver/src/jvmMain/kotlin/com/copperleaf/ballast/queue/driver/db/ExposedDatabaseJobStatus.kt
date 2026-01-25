package com.copperleaf.ballast.queue.driver.db

public enum class ExposedDatabaseJobStatus {

    /**
     * The job is available to be selected once it has reached its scheduled time.
     */
    Pending,

    /**
     * The job has been selected for processing. It is now held exclusively by one worker, and is under a lease. If the
     * worker crashes during processing, the job will be returned to Pending once the lease expires, assuming it has
     * retries left.
     */
    Running,

    /**
     * The job has completed successfully. It is eligible to be deleted from the database as a maintenance task.
     */
    Succeeded,

    /**
     * The job has failed permanently, with no retries left. It should be considered dead, and should be reported as a
     * catastrophic failure which needs human intervention, without which it will not be possible to complete this job.
     *
     * Failed jobs should not be automatically deleted from the database, as they represent important failure cases
     * which need to be addressed, and perhaps scheduled for retry once a fix is in place.
     */
    Failed,

    /**
     * This was a unique job which completed successfully, and is now in a "cooldown" phase. No other jobs with the
     * same deduplication key can be scheduled until this cooldown period has expired.
     */
    Cooldown,

    /**
     * This is an ephemeral state used to request cancallation of the job. By changing a job's status to Cancelled while
     * it is running, it signals to the worker processing the job that it should halt processing as soon as possible.
     *
     * Cancellation is not guaranteed, but is a best-effort attempt to stop processing the job. Once a job is marked
     * as Cancelled, it will be treated like a timeout or exception failure for purposes of retrys and backoff, assuming
     * it has retries left.
     */
    Cancelled;
}
