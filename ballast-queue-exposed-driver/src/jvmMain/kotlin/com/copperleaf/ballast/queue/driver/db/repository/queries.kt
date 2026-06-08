package com.copperleaf.ballast.queue.driver.db.repository

import com.copperleaf.ballast.queue.SerializedJob
import com.copperleaf.ballast.queue.driver.db.ExposedDatabaseJobStatus
import com.copperleaf.ballast.queue.driver.db.ExposedDatabaseQueueDriver
import com.copperleaf.ballast.queue.driver.db.JobsTable
import org.jetbrains.exposed.v1.core.Case
import org.jetbrains.exposed.v1.core.LiteralOp
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.greater
import org.jetbrains.exposed.v1.core.isNull
import org.jetbrains.exposed.v1.core.less
import org.jetbrains.exposed.v1.core.or
import org.jetbrains.exposed.v1.core.plus
import org.jetbrains.exposed.v1.core.statements.UpdateStatement
import org.jetbrains.exposed.v1.datetime.CurrentTimestamp
import kotlin.time.Clock

internal fun JobsTable.retryOrFailStatusColumn(update: UpdateStatement) {
    update[status] = Case()
        .When(
            // Retry if: attempts remaining AND (no deadline OR deadline is still in the future).
            // retry_until is a "do not retry after" deadline — so retry while it is still ahead of us.
            cond = (attempts less max_attempts) and
                    ((retry_until.isNull()) or
                            (retry_until greater CurrentTimestamp)),
            result = LiteralOp(status.columnType, ExposedDatabaseJobStatus.Pending)
        )
        .Else(
            LiteralOp(status.columnType, ExposedDatabaseJobStatus.Failed)
        )
}

internal fun mapResultRowToSerializedJob(
    table: JobsTable,
    resultRow: ResultRow,
): SerializedJob<ExposedDatabaseQueueDriver.Metadata> {
    return SerializedJob(
        jobId = resultRow[table.id].value.toString(),
        queueName = resultRow[table.queue],
        serializedPayload = resultRow[table.payload].toString(),
        timeoutDuration = resultRow[table.timeout_duration],
        serializedState = resultRow[table.job_state].toString(),
        serializedResultData = resultRow[table.result_data]?.toString(),
        attempts = resultRow[table.attempts],
        metadata = ExposedDatabaseQueueDriver.Metadata(
            insertedAt = resultRow[table.created_at],
            maxAttempts = resultRow[table.max_attempts],
            retryUntil = resultRow[table.retry_until],
            deduplicationKey = resultRow[table.deduplication_key],
            deduplicationDuration = resultRow[table.deduplication_duration],
            messageGroup = resultRow[table.message_group],
            priority = resultRow[table.priority],
            runAt = resultRow[table.run_at],
            status = resultRow[table.status],
            leasedAt = resultRow[table.leased_at],
            leaseBufferDuration = resultRow[table.lease_buffer_duration],
            leasedUntil = resultRow[table.leased_until],
            lastRunFinishedAt = resultRow[table.last_run_finished_at],
            lastRunDuration = resultRow[table.last_run_duration],
            lastResultType = resultRow[table.last_run_result_type],
            lastErrorMessage = resultRow[table.last_run_failure_message],
            lastStacktrace = resultRow[table.last_run_failure_stacktrace],
        ),
    )
}

internal fun JobsTable.moveToDeadLetterQueue(
    update: UpdateStatement,
    deadLetterQueueName: String,
    clock: Clock,
) {
    update[this.queue] = deadLetterQueueName
    update[this.status] = ExposedDatabaseJobStatus.Pending

    // give the job one more attempt, intended for the DLQ processor to handle. The DLQ must be able to
    // successfully report on the failed job with a single attempt, so failed jobs don't get stuck forever
    // in the DLQ
    update[run_at] = clock.now()
    update[max_attempts] = max_attempts + 1
    update[original_queue] = queue
}
