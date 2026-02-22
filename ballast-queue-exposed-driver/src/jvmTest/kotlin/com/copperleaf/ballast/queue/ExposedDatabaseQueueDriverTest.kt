package com.copperleaf.ballast.queue

import com.copperleaf.ballast.queue.driver.db.ExposedDatabaseQueueDriver
import com.copperleaf.ballast.queue.driver.db.JobsTable
import com.copperleaf.ballast.queue.driver.db.repository.JobsRepositoryImpl
import com.copperleaf.ballast.scheduler.TestClock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import org.jetbrains.exposed.v1.core.StdOutSqlLogger
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.time.Duration.Companion.seconds

class ExposedDatabaseQueueDriverTest : BaseDatabaseTest() {

// Test Setup
// ---------------------------------------------------------------------------------------------------------------------

    val timezone = TimeZone.UTC
    val startInstant = LocalDate(2025, 1, 1).atStartOfDayIn(timezone)

// Tests
// ---------------------------------------------------------------------------------------------------------------------

    @Test
    fun addToQueueTest_success() = runTestWithDatabase {
        val clock = testScope.TestClock(startInstant)
        val table = JobsTable.Default
        val repository = JobsRepositoryImpl(database, table, clock)
        val driver = ExposedDatabaseQueueDriver(repository)

        suspendTransaction(database) {
            addLogger(StdOutSqlLogger)

            driver.addToQueue(
                queueName = "test-queue",
                serializedPayload = """{"type":"TestJob","data":{"value":42}}""",
                serializedInitialState = """{"type":"TestJob","data":{"value":42}}""",
                timeoutDuration = 30.seconds,
                metadata = ExposedDatabaseQueueDriver.Metadata(
                    insertedAt = clock.now(),
                    maxAttempts = 5,
                )
            )

            table.assertJobEquals(
                rows = table.selectAll().toList(),
                expected = listOf(
                    SerializedJob(
                        jobId = "", // ID is ignored
                        queueName = "test-queue",
                        serializedPayload = """{"type":"TestJob","data":{"value":42}}""",
                        timeoutDuration = 30.seconds,
                        serializedState = """{"type":"TestJob","data":{"value":42}}""",
                        serializedResultData = null,
                        metadata = ExposedDatabaseQueueDriver.Metadata(
                            insertedAt = clock.now(),
                            maxAttempts = 5,
                        ),
                    )
                )
            )
        }
    }

    @Test
    fun insertAndUpdate() = runTestWithDatabase {
        val clock = testScope.TestClock(startInstant)
        val table = JobsTable.Default
        val repository = JobsRepositoryImpl(database, table, clock)
        val driver = ExposedDatabaseQueueDriver(repository)

        suspendTransaction(database) {
            addLogger(StdOutSqlLogger)

            driver.addToQueue(
                queueName = "test-queue",
                serializedPayload = """{"type":"TestJob","data":{"value":42}}""",
                serializedInitialState = """{"type":"TestJob","data":{"value":42}}""",
                timeoutDuration = 30.seconds,
                metadata = ExposedDatabaseQueueDriver.Metadata(
                    insertedAt = clock.now(),
                    maxAttempts = 5,
                )
            )
        }

        suspendTransaction(database) {
            table.assertJobEquals(
                rows = table.selectAll().toList(),
                expected = listOf(
                    SerializedJob(
                        jobId = "", // ID is ignored
                        queueName = "test-queue",
                        serializedPayload = """{"type":"TestJob","data":{"value":42}}""",
                        timeoutDuration = 30.seconds,
                        serializedState = """{"type":"TestJob","data":{"value":42}}""",
                        serializedResultData = null,
                        metadata = ExposedDatabaseQueueDriver.Metadata(
                            insertedAt = clock.now(),
                            maxAttempts = 5,
                        ),
                    )
                )
            )
        }
    }
}
