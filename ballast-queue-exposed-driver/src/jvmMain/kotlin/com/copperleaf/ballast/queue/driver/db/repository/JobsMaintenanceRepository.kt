package com.copperleaf.ballast.queue.driver.db.repository

import com.copperleaf.ballast.queue.driver.db.ExposedDatabaseJobStatus.Cancelled
import com.copperleaf.ballast.queue.driver.db.ExposedDatabaseJobStatus.Cooldown
import com.copperleaf.ballast.queue.driver.db.ExposedDatabaseJobStatus.Failed
import com.copperleaf.ballast.queue.driver.db.ExposedDatabaseJobStatus.Pending
import com.copperleaf.ballast.queue.driver.db.ExposedDatabaseJobStatus.Running
import com.copperleaf.ballast.queue.driver.db.ExposedDatabaseJobStatus.Succeeded
import com.copperleaf.ballast.queue.driver.db.ExposedDatabaseQueueDriver.Metadata
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days

public interface JobsMaintenanceRepository {
    /**
     * Deletes all jobs that have been in the [Succeeded] state for longer than the given [duration].
     */
    public suspend fun deleteOldJobs(duration: Duration = 30.days)

    /**
     * Moves all Unique jobs in the [Cooldown] state to [Succeeded] if their cooldown period has expired, allowing
     * another job at the same [Metadata.deduplicationKey]` to be inserted into the queue.
     */
    public suspend fun freeJobCooldowns()

    /**
     * Moves all jobs in the [Running] or [Cancelled] state whose lease has expired back to [Pending], so they can be
     * retried, or moved to [Failed] if they are not eligible for retry.
     */
    public suspend fun retryHungJobs()

    /**
     * Moves all jobs in the [Failed] state to the given [deadLetterQueueName], so the permanent failure can be
     * reported and inspected. It's assumed that the DLQ will do little more than log an error or trigger an alert
     * to notify operators of the failure, so they issue can be addressed. If [originalQueueName] is non-null, then
     * only the jobs from that queue will be sep
     *
     * Once the root issue has been resolved, jobs can be moved back from the DLQ to their original queue for
     * reprocessing with [moveFromDeadLetterQueue].
     */
    public suspend fun moveToDeadLetterQueue(deadLetterQueueName: String, originalQueueName: String? = null)

    /**
     * Moves jobs in the [Succeeded] state from the Dead Letter Queue with the given [deadLetterQueueName] back to their
     * original queue, indicating that the issue causing the jobs to fail has been addressed and they are ready to be
     * reprocessed. If [originalQueueName] is provided, only jobs from that original queue will be moved back;
     * otherwise, all jobs in the dead letter queue will be moved back to their respective original queues.
     *
     * When moved back to the original queue, they are granted an additional number of attempts specified by
     * [additionalAttempts] to allow for successful processing and retries
     */
    public suspend fun moveFromDeadLetterQueue(
        deadLetterQueueName: String,
        originalQueueName: String?,
        additionalAttempts: Int = 5,
    )

    /**
     * Sometimes, messages sent to the DLQ are determined to be non-recoverable and should be deleted entirely.
     * This function deletes jobs from the specified [deadLetterQueueName]. If [originalQueueName] is provided,
     * only jobs that originated from that queue will be deleted; otherwise, all jobs in the dead letter queue will be
     * deleted.
     */
    public suspend fun deleteFromDeadLetterQueue(
        deadLetterQueueName: String,
        originalQueueName: String?,
    )
}
