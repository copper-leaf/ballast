package com.copperleaf.ballast.queue

import com.copperleaf.ballast.queue.driver.db.ExposedDatabaseQueueDriver
import com.copperleaf.ballast.queue.driver.db.JobsTable
import com.copperleaf.ballast.queue.driver.db.repository.JobsRepositoryImpl
import com.copperleaf.ballast.scheduler.TestClock
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import org.jetbrains.exposed.v1.core.StdOutSqlLogger
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.deleteAll
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.time.Duration.Companion.seconds

@Ignore
class ExposedDatabaseQueueDriverTest {

// Test Setup
// ---------------------------------------------------------------------------------------------------------------------

    lateinit var database: Database
    lateinit var table: JobsTable

    val timezone = TimeZone.UTC
    val startInstant = LocalDate(2025, 1, 1).atStartOfDayIn(timezone)

    @BeforeTest
    fun setup() {
        database = Database.connect(
            "jdbc:postgresql://localhost:5432/postgres",
            driver = "org.postgresql.Driver",
            user = "postgres",
            password = "postgres"
        )
        table = JobsTable.Default
    }

    @AfterTest
    fun teardown(): Unit = runBlocking {
        suspendTransaction(database) {
            table.deleteAll()
        }
    }

// Tests
// ---------------------------------------------------------------------------------------------------------------------

    @Test
    fun addToQueueTest_success() = runTest {
        val clock = TestClock(startInstant)
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
    fun insertAndUpdate() = runTest {
        val clock = TestClock(startInstant)
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
