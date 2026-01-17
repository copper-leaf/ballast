package com.copperleaf.ballast.queue.driver

import com.copperleaf.ballast.queue.JobCompletionResultType
import com.copperleaf.ballast.queue.JobStatus
import com.copperleaf.ballast.scheduler.TestClock
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalCoroutinesApi::class)
class InMemoryQueueDriverTest {

    @Test
    fun enqueueAndPollNext() = runTest {
        val timezone = TimeZone.UTC
        val startInstant = LocalDate(2025, 1, 1).atStartOfDayIn(timezone)
        val clock = TestClock(startInstant)
        val driver = InMemoryQueueDriver(clock)

        val uuid = driver.addToQueue(
            queueName = "one",
            serializedPayload = "{}",
            timeoutDuration = 30.seconds,
            metadata = InMemoryQueueDriver.Metadata(
                insertedAt = clock.now(),
                priority = 0,
                runAt = clock.now() + 1.minutes,
                maxAttempts = 5,
                attempts = 0,
                lastRunDuration = null,
            )
        )

        // no jobs ready yet, since runAt is in the future
        driver.pollNext("one").let {
            assertNull(it)
        }
        driver.observeJobState(uuid).firstOrNull().let {
            assertNotNull(it)
            assertEquals(
                actual = it.status,
                expected = JobStatus.Pending,
            )
        }

        advanceTimeBy(2.minutes)

        // the job is ready, but only in the intended Queue
        driver.pollNext("two").let {
            assertNull(it)
        }
        driver.pollNext("one").let {
            assertNotNull(it)
        }

        // because we received the job from observeQueue(), its status is now Running
        driver.observeJobState(uuid).firstOrNull().let {
            assertNotNull(it)
            assertEquals(
                actual = it.status,
                expected = JobStatus.Running,
            )
        }
    }

    @Test
    fun failJobAndRetry() = runTest {
        val timezone = TimeZone.UTC
        val startInstant = LocalDate(2025, 1, 1).atStartOfDayIn(timezone)
        val clock = TestClock(startInstant)
        val driver = InMemoryQueueDriver(clock)

        val uuid = driver.addToQueue(
            queueName = "one",
            serializedPayload = "{}",
            timeoutDuration = 30.seconds,
            metadata = InMemoryQueueDriver.Metadata(
                insertedAt = clock.now(),
                priority = 0,
                runAt = clock.now(),
                maxAttempts = 5,
                attempts = 0,
                lastRunDuration = null,
            )
        )

        assertNotNull(driver.pollNext("one"))

        // because we received the job from observeQueue(), its status is now Running
        assertEquals(
            actual = driver.observeJobState(uuid).firstOrNull()?.status,
            expected = JobStatus.Running,
        )

        // mark job completion as a failure
        driver.markJobCompleted(
            jobId = uuid,
            processingTime = 5.seconds,
            resultType = JobCompletionResultType.Failure,
            serializedResultData = JsonObject(mapOf("error" to JsonPrimitive("testError"))).toString(),
            retryDelay = null,
        )

        // job gets re-enqueued because it still had retries left
        driver.observeJobState(uuid).firstOrNull().let {
            assertEquals(
                actual = it?.status,
                expected = JobStatus.Pending,
            )
            assertEquals(
                actual = it?.metadata?.lastRunDuration,
                expected = 5.seconds,
            )
            assertEquals(
                actual = it?.serializedResultData,
                expected = JsonObject(mapOf("error" to JsonPrimitive("testError"))).toString(),
            )
        }
    }

    @Test
    fun failJobAndPermanentlyFail() = runTest {
        val timezone = TimeZone.UTC
        val startInstant = LocalDate(2025, 1, 1).atStartOfDayIn(timezone)
        val clock = TestClock(startInstant)
        val driver = InMemoryQueueDriver(clock)

        val uuid = driver.addToQueue(
            queueName = "one",
            serializedPayload = "{}",
            timeoutDuration = 30.seconds,
            metadata = InMemoryQueueDriver.Metadata(
                insertedAt = clock.now(),
                priority = 0,
                runAt = clock.now(),
                maxAttempts = 5,
                attempts = 4,
                lastRunDuration = null,
            )
        )

        assertNotNull(driver.pollNext("one"))

        // because we received the job from observeQueue(), its status is now Running
        assertEquals(
            actual = driver.observeJobState(uuid).firstOrNull()?.status,
            expected = JobStatus.Running,
        )

        // mark job completion as a failure
        driver.markJobCompleted(
            jobId = uuid,
            processingTime = 5.seconds,
            resultType = JobCompletionResultType.Failure,
            serializedResultData = JsonObject(mapOf("error" to JsonPrimitive("testError"))).toString(),
            retryDelay = null,
        )

        // job gets marked as Failed because it was on its last retry
        driver.observeJobState(uuid).firstOrNull().let {
            assertEquals(
                actual = it?.status,
                expected = JobStatus.Failed,
            )
            assertEquals(
                actual = it?.metadata?.lastRunDuration,
                expected = 5.seconds,
            )
            assertEquals(
                actual = it?.serializedResultData,
                expected = JsonObject(mapOf("error" to JsonPrimitive("testError"))).toString(),
            )
        }
    }
}
