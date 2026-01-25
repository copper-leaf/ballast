package com.copperleaf.ballast.queue.driver.db

import com.copperleaf.ballast.queue.SerializedJob
import org.jetbrains.exposed.v1.core.ResultRow

internal object SerializedJobMapper {
    fun mapResultRowToSerializedJob(
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
                deduplicationKey = resultRow[table.deduplication_key],
                deduplicationDuration = resultRow[table.deduplication_duration],
                priority = resultRow[table.priority],
                runAt = resultRow[table.run_at],
                status = resultRow[table.status],
                leasedAt = resultRow[table.leased_at],
                leasedUntil = resultRow[table.leased_until],
                lastRunFinishedAt = resultRow[table.last_run_finished_at],
                lastRunDuration = resultRow[table.last_run_duration],
                lastResultType = resultRow[table.last_run_result_type],
                lastErrorMessage = resultRow[table.last_run_failure_message],
                lastStacktrace = resultRow[table.last_run_failure_stacktrace],
            ),
        )
    }
}
