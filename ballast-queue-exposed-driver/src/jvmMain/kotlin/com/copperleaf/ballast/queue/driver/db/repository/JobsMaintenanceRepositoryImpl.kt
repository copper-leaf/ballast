package com.copperleaf.ballast.queue.driver.db.repository

import com.copperleaf.ballast.queue.driver.db.ExposedDatabaseJobStatus
import com.copperleaf.ballast.queue.driver.db.JobsTable
import com.copperleaf.ballast.queue.driver.db.TimestampAdd
import org.jetbrains.exposed.v1.core.Op
import org.jetbrains.exposed.v1.core.SqlLogger
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.inList
import org.jetbrains.exposed.v1.core.lessEq
import org.jetbrains.exposed.v1.core.plus
import org.jetbrains.exposed.v1.core.vendors.currentDialect
import org.jetbrains.exposed.v1.datetime.CurrentTimestamp
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import org.jetbrains.exposed.v1.jdbc.update
import kotlin.time.Clock
import kotlin.time.Duration

public class JobsMaintenanceRepositoryImpl(
    private val database: Database,
    private val table: JobsTable = JobsTable.Default,
    private val clock: Clock = Clock.System,
    private val logger: SqlLogger? = null,
) : JobsMaintenanceRepository {

    private suspend fun <T> withTransaction(log: Boolean = true, block: suspend () -> T): T {
        return suspendTransaction(database) {
            if (log && logger != null) {
                addLogger(logger)
            }
            block()
        }
    }

    override suspend fun deleteOldJobs(duration: Duration) {
        withTransaction {
            table.deleteWhere {
                (table.status eq ExposedDatabaseJobStatus.Succeeded) and
                        (TimestampAdd(last_run_finished_at, duration, currentDialect) lessEq CurrentTimestamp)
            }
        }
    }

    override suspend fun freeJobCooldowns() {
        withTransaction {
            table.update({
                (table.status eq ExposedDatabaseJobStatus.Cooldown) and
                        (table.unique_until lessEq CurrentTimestamp)
            }) {
                it[table.status] = ExposedDatabaseJobStatus.Succeeded
            }
        }
    }

    override suspend fun retryHungJobs() {
        withTransaction {
            table.update({
                (table.status inList listOf(ExposedDatabaseJobStatus.Running, ExposedDatabaseJobStatus.Cancelled)) and
                        (table.leased_until lessEq CurrentTimestamp)
            }) {
                retryOrFailStatusColumn(it)
            }
        }
    }

    override suspend fun moveToDeadLetterQueue(deadLetterQueueName: String) {
        withTransaction {
            table.update({
                table.status eq ExposedDatabaseJobStatus.Failed
            }) {
                it[table.queue] = deadLetterQueueName
                it[table.status] = ExposedDatabaseJobStatus.Pending

                // give the job one more attempt, intended for the DLQ processor to handle. The DLQ must be able to
                // successfully report on the failed job with a single attempt, so failed jobs don't get stuck forever
                // in the DLQ
                it[run_at] = clock.now()
                it[max_attempts] = max_attempts + 1
                it[original_queue] = queue
            }
        }
    }

    override suspend fun moveFromDeadLetterQueue(
        deadLetterQueueName: String,
        originalQueueName: String?,
        additionalAttempts: Int,
    ) {
        withTransaction {
            table.update({
                (table.queue eq deadLetterQueueName) and
                        (if (originalQueueName != null) table.original_queue eq originalQueueName else Op.TRUE)
            }) {
                it[table.queue] = original_queue
                it[table.status] = ExposedDatabaseJobStatus.Pending

                it[run_at] = clock.now()
                it[max_attempts] = max_attempts + additionalAttempts
                it[retry_until] = null
                it[table.original_queue] = null
            }
        }
    }

    override suspend fun deleteFromDeadLetterQueue(
        deadLetterQueueName: String,
        originalQueueName: String?
    ) {
        withTransaction {
            table.deleteWhere {
                (table.queue eq deadLetterQueueName) and
                        (if (originalQueueName != null) table.original_queue eq originalQueueName else Op.TRUE)
            }
        }
    }
}
