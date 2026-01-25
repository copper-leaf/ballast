package com.copperleaf.ballast.queue.driver.db.repository

import com.copperleaf.ballast.queue.JobCompletionResultType
import com.copperleaf.ballast.queue.SerializedJob
import com.copperleaf.ballast.queue.driver.db.ExposedDatabaseJobStatus
import com.copperleaf.ballast.queue.driver.db.ExposedDatabaseQueueDriver
import com.copperleaf.ballast.queue.driver.db.JobsTable
import com.copperleaf.ballast.queue.driver.db.SerializedJobMapper
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.v1.core.Case
import org.jetbrains.exposed.v1.core.LiteralOp
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.SqlLogger
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.greater
import org.jetbrains.exposed.v1.core.isNotNull
import org.jetbrains.exposed.v1.core.less
import org.jetbrains.exposed.v1.core.lessEq
import org.jetbrains.exposed.v1.core.plus
import org.jetbrains.exposed.v1.core.vendors.ForUpdateOption
import org.jetbrains.exposed.v1.core.vendors.MysqlDialect
import org.jetbrains.exposed.v1.core.vendors.PostgreSQLDialect
import org.jetbrains.exposed.v1.core.vendors.currentDialect
import org.jetbrains.exposed.v1.datetime.CurrentTimestamp
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insertAndGetId
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import org.jetbrains.exposed.v1.jdbc.update
import org.jetbrains.exposed.v1.jdbc.updateReturning
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.uuid.Uuid

public class JobsRepositoryImpl(
    private val database: Database,
    private val clock: Clock = Clock.System,
    private val table: JobsTable = JobsTable.Default,
    private val json: Json = Json.Default,
    private val logger: SqlLogger? = null,
) : JobsRepository {

    private suspend fun <T> withTransaction(log: Boolean = true, block: suspend () -> T): T {
        return suspendTransaction(database) {
            if (log && logger != null) {
                addLogger(logger)
            }
            block()
        }
    }

    override suspend fun getAllJobs(): List<SerializedJob<ExposedDatabaseQueueDriver.Metadata>> {
        return withTransaction(false) {
            table
                .select(table.columns)
                .map { resultRow ->
                    SerializedJobMapper.mapResultRowToSerializedJob(
                        table,
                        resultRow,
                    )
                }
        }
    }

    override suspend fun getAllJobsInQueue(queueName: String): List<SerializedJob<ExposedDatabaseQueueDriver.Metadata>> {
        return withTransaction {
            table
                .select(table.columns)
                .where { table.queue eq queueName }
                .map { resultRow ->
                    SerializedJobMapper.mapResultRowToSerializedJob(
                        table,
                        resultRow,
                    )
                }
        }
    }

// Claim job
// ---------------------------------------------------------------------------------------------------------------------

    override suspend fun claimNextAvailableJob(
        queueName: String,
        leaseBufferDuration: Duration,
    ): SerializedJob<ExposedDatabaseQueueDriver.Metadata>? {
        // assumes an existing database in transaction from the caller. But we need a sub-transaction here to do
        // the FOR UPDATE SKIP LOCKED
        return withTransaction(false) {
            when (currentDialect) {
                is PostgreSQLDialect -> {
                    claimNextAvailableJobForPostgres(
                        queueName,
                        leaseBufferDuration,
                    )
                }
                is MysqlDialect -> {
                    claimNextAvailableJobForMysql(
                        queueName,
                        leaseBufferDuration,
                    )
                }
                else -> {
                    error("Unsupported database dialect: $currentDialect")
                }
            }
        }
    }

    private suspend fun claimNextAvailableJobForPostgres(
        queueName: String,
        leaseBufferDuration: Duration,
    ): SerializedJob<ExposedDatabaseQueueDriver.Metadata>? {
        // assumes an existing database in transaction from the caller. But we need a sub-transaction here to do
        // the FOR UPDATE SKIP LOCKED

        val now = clock.now()

        // Step 1: Find the next eligible job with FOR UPDATE SKIP LOCKED to ensure jobs are selected exactly once
        val initialResultRow = table
            .select(table.columns)
            .where {
                (table.queue eq queueName) and
                        (table.status eq ExposedDatabaseJobStatus.Pending) and
                        (table.run_at lessEq now)
            }
            .orderBy(
                table.priority to SortOrder.DESC,
                table.run_at to SortOrder.DESC,
            )
            .forUpdate(ForUpdateOption.PostgreSQL.ForUpdate(ForUpdateOption.PostgreSQL.MODE.SKIP_LOCKED))
            .limit(1)
            .singleOrNull()
            ?: return@claimNextAvailableJobForPostgres null

        // Step 2: Update the job to mark it as in-progress, and return the updated job row
        val resultRow = table
            .updateReturning(
                returning = table.columns,
                where = { table.id eq initialResultRow[table.id].value },
                body = {
                    it[status] = ExposedDatabaseJobStatus.Running
                    it[attempts] = initialResultRow[table.attempts] + 1
                    it[leased_at] = now
                    it[leased_until] = now + initialResultRow[table.timeout_duration] + leaseBufferDuration
                }
            )
            .single()

        // Step 3: map the selected row to SerializedJob
        return SerializedJobMapper.mapResultRowToSerializedJob(
            table,
            resultRow,
        )
    }

    private suspend fun claimNextAvailableJobForMysql(
        queueName: String,
        leaseBufferDuration: Duration,
    ): SerializedJob<ExposedDatabaseQueueDriver.Metadata>? {

        val now = clock.now()

        // Step 1: Find the next eligible job with FOR UPDATE SKIP LOCKED to ensure jobs are selected exactly once
        val initialResultRow = table
            .select(table.columns)
            .where {
                (table.queue eq queueName) and
                        (table.status eq ExposedDatabaseJobStatus.Pending) and
                        (table.run_at lessEq now)
            }
            .orderBy(
                table.priority to SortOrder.DESC,
                table.run_at to SortOrder.DESC,
            )
            .forUpdate(ForUpdateOption.MySQL.ForUpdate(ForUpdateOption.MySQL.MODE.SKIP_LOCKED))
            .limit(1)
            .singleOrNull()
            ?: return null

        // Step 2: Update the job to mark it as in-progress, and return the updated job row
        table
            .update(
                where = { table.id eq initialResultRow[table.id].value },
                body = {
                    it[status] = ExposedDatabaseJobStatus.Running
                    it[attempts] = initialResultRow[table.attempts] + 1
                    it[leased_at] = now
                    it[leased_until] = now + initialResultRow[table.timeout_duration] + leaseBufferDuration
                }
            )

        val resultRow = table
            .select(table.columns)
            .where { table.id eq initialResultRow[table.id].value }
            .limit(1)
            .single()

        // Step 3: map the selected row to SerializedJob
        return SerializedJobMapper.mapResultRowToSerializedJob(
            table,
            resultRow,
        )
    }

// Insert job
// ---------------------------------------------------------------------------------------------------------------------

    override suspend fun insertJob(
        queueName: String,
        serializedPayload: String,
        serializedInitialState: String,
        timeoutDuration: Duration,
        metadata: ExposedDatabaseQueueDriver.Metadata,
    ): Uuid {
        return withTransaction {
            table.insertAndGetId {
                it[table.queue] = queueName
                it[table.payload] = json.parseToJsonElement(serializedPayload)
                it[table.job_state] = json.parseToJsonElement(serializedInitialState)
                it[table.priority] = metadata.priority
                it[table.run_at] = metadata.runAt
                it[table.max_attempts] = metadata.maxAttempts
                it[table.timeout_duration] = timeoutDuration

                if (metadata.deduplicationKey != null) {
                    requireNotNull(metadata.deduplicationDuration)
                    it[table.deduplication_key] = metadata.deduplicationKey
                    it[table.unique_until] = metadata.runAt + metadata.deduplicationDuration
                } else {
                    it[table.deduplication_key] = null
                    it[table.unique_until] = null
                }
            }.value
        }
    }

    override suspend fun completeJob(
        jobId: Uuid,
        processingTime: Duration,
        resultType: JobCompletionResultType,
        serializedResultData: String?,
    ) {
        withTransaction {
            table.update({ table.id eq jobId }) {
                it[table.status] = Case()
                    .When(
                        cond = table.unique_until.isNotNull() and (table.unique_until greater CurrentTimestamp),
                        result = LiteralOp(table.status.columnType, ExposedDatabaseJobStatus.Cooldown),
                    )
                    .Else(
                        LiteralOp(table.status.columnType, ExposedDatabaseJobStatus.Succeeded)
                    )

                it[leased_at] = null
                it[leased_until] = null

                it[table.result_data] = serializedResultData?.let { data -> json.parseToJsonElement(data) }

                it[table.last_run_result_type] = resultType
                it[table.last_run_duration] = processingTime
                it[table.last_run_finished_at] = clock.now()
                it[table.last_run_failure_message] = null
                it[table.last_run_failure_stacktrace] = null
            }
        }
    }

    override suspend fun retryOrPermanentlyFailJob(
        jobId: Uuid,
        processingTime: Duration,
        resultType: JobCompletionResultType,
        retryDelay: Duration,
        permanentlyFail: Boolean,
        failureMessage: String?,
        failureStacktrace: String?,
    ) {
        withTransaction {
            table.update({ table.id eq jobId }) {
                if (permanentlyFail) {
                    it[table.status] = ExposedDatabaseJobStatus.Failed
                } else {
                    it[table.status] = Case()
                        .When(
                            cond = table.attempts less table.max_attempts,
                            result = LiteralOp(table.status.columnType, ExposedDatabaseJobStatus.Pending)
                        )
                        .Else(
                            LiteralOp(table.status.columnType, ExposedDatabaseJobStatus.Failed)
                        )
                    it[run_at] = clock.now() + retryDelay
                }

                it[leased_at] = null
                it[leased_until] = null

                it[table.result_data] = null

                it[table.last_run_result_type] = resultType
                it[table.last_run_duration] = processingTime
                it[table.last_run_finished_at] = clock.now()
                it[table.last_run_failure_message] = failureMessage
                it[table.last_run_failure_stacktrace] = failureStacktrace
            }
        }
    }

    override suspend fun setJobState(
        jobId: Uuid,
        serializedState: String,
    ) {
        withTransaction {
            table.update({ table.id eq jobId }) {
                it[table.job_state] = json.parseToJsonElement(serializedState)
            }
        }
    }

    override suspend fun requestCancellation(jobId: Uuid) {
        withTransaction {
            table.update({ table.id eq jobId }) {
                it[table.status] = ExposedDatabaseJobStatus.Cancelled
            }
        }
    }

    override suspend fun isJobCancelled(jobId: Uuid): Boolean {
        return withTransaction(false) {
            val jobStatus = table
                .select(table.id, table.status)
                .where { table.id eq jobId }
                .withDistinct()
                .limit(1)
                .singleOrNull()
                ?.let { it[table.status] }

            if (jobStatus == null) {
                // the row was deleted, cancel the job
                true
            } else if (jobStatus == ExposedDatabaseJobStatus.Cancelled) {
                // the row was manually cancelled, cancel the job
                true
            } else {
                false
            }
        }
    }

    override suspend fun deleteJob(jobId: Uuid) {
        return withTransaction(false) {
            table.deleteWhere { table.id eq jobId }
        }
    }

    override suspend fun forceRetry(
        jobId: Uuid,
        retryDelay: Duration,
        additionalAttempts: Int,
    ) {
        withTransaction {
            table.update({ table.id eq jobId }) {
                it[table.status] = ExposedDatabaseJobStatus.Pending

                it[run_at] = clock.now() + retryDelay
                it[max_attempts] = max_attempts + 1
            }
        }
    }
}
