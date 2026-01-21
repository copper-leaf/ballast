package com.copperleaf.ballast.queue.driver.db.repository

import com.copperleaf.ballast.queue.driver.DatabaseJobStatus
import com.copperleaf.ballast.queue.driver.JobsTable
import com.copperleaf.ballast.queue.driver.db.TimestampAdd
import org.jetbrains.exposed.v1.core.SqlLogger
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.lessEq
import org.jetbrains.exposed.v1.core.vendors.currentDialect
import org.jetbrains.exposed.v1.datetime.CurrentTimestamp
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import org.jetbrains.exposed.v1.jdbc.update
import kotlin.time.Duration

public class JobsMaintenanceRepositoryImpl(
    private val database: Database,
    private val table: JobsTable = JobsTable.Default,
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
                (table.status eq DatabaseJobStatus.Succeeded) and
                        (TimestampAdd(last_run_finished_at, duration, currentDialect) lessEq CurrentTimestamp)
            }
        }
    }

    override suspend fun freeJobCooldowns() {
        withTransaction {
            table.update({
                (table.status eq DatabaseJobStatus.Cooldown) and
                        (table.unique_until lessEq CurrentTimestamp)
            }) {
                it[table.status] = DatabaseJobStatus.Succeeded
            }
        }
    }

    override suspend fun retryHungJobs() {
        withTransaction {
            table.update({
                (table.status eq DatabaseJobStatus.Running) and
                        (table.leased_until lessEq CurrentTimestamp)
            }) {
                it[table.status] = DatabaseJobStatus.Pending
            }
        }
    }
}
