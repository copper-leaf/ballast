package com.copperleaf.ballast.queue.driver

import com.copperleaf.ballast.queue.JobCompletionResultType
import com.copperleaf.ballast.queue.driver.memory.InMemoryJobStatus
import com.copperleaf.ballast.queue.driver.memory.InMemoryQueueDriver
import com.copperleaf.ballast.scheduler.TestClock
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
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
            serializedInitialState = "{}",
            timeoutDuration = 30.seconds,
            metadata = InMemoryQueueDriver.Metadata(
                insertedAt = clock.now(),
                priority = 0,
                runAt = clock.now() + 1.minutes,
                maxAttempts = 5,
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
                actual = it.metadata.status,
                expected = InMemoryJobStatus.Pending,
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
                actual = it.metadata.status,
                expected = InMemoryJobStatus.Running,
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
            serializedInitialState = "{}",
            timeoutDuration = 30.seconds,
            metadata = InMemoryQueueDriver.Metadata(
                insertedAt = clock.now(),
                priority = 0,
                runAt = clock.now(),
                maxAttempts = 5,
                lastRunDuration = null,
            )
        )

        assertNotNull(driver.pollNext("one"))

        // because we received the job from observeQueue(), its status is now Running
        assertEquals(
            actual = driver.observeJobState(uuid).firstOrNull()?.metadata?.status,
            expected = InMemoryJobStatus.Running,
        )

        // mark job completion as a failure
        driver.completeJobWithFailure(
            jobId = uuid,
            processingTime = 5.seconds,
            resultType = JobCompletionResultType.Failure,
            retryDelay = 30.seconds,
            permanentlyFail = false,
            failureMessage = "testError",
            failureStacktrace = null,
            skipAttempt = false,
        )

        // job gets re-enqueued because it still had retries left
        driver.observeJobState(uuid).firstOrNull().let {
            assertEquals(
                actual = it?.metadata?.status,
                expected = InMemoryJobStatus.Pending,
            )
            assertEquals(
                actual = it?.metadata?.lastRunDuration,
                expected = 5.seconds,
            )
            assertEquals(
                actual = it?.serializedResultData,
                expected = null,
            )
            assertEquals(
                actual = it?.metadata?.lastErrorMessage,
                expected = "testError",
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
            serializedInitialState = "{}",
            timeoutDuration = 30.seconds,
            metadata = InMemoryQueueDriver.Metadata(
                insertedAt = clock.now(),
                priority = 0,
                runAt = clock.now(),
                maxAttempts = 1,
                lastRunDuration = null,
            )
        )

        assertNotNull(driver.pollNext("one"))

        // because we received the job from observeQueue(), its status is now Running
        assertEquals(
            actual = driver.observeJobState(uuid).firstOrNull()?.metadata?.status,
            expected = InMemoryJobStatus.Running,
        )

        // mark job completion as a failure
        driver.completeJobWithFailure(
            jobId = uuid,
            processingTime = 5.seconds,
            resultType = JobCompletionResultType.Failure,
            retryDelay = 30.seconds,
            permanentlyFail = false,
            failureMessage = "testError",
            failureStacktrace = null,
            skipAttempt = false,
        )

        // job gets marked as Failed because it was on its last retry
        driver.observeJobState(uuid).firstOrNull().let {
            assertEquals(
                actual = it?.metadata?.status,
                expected = InMemoryJobStatus.Failed,
            )
            assertEquals(
                actual = it?.metadata?.lastRunDuration,
                expected = 5.seconds,
            )
            assertEquals(
                actual = it?.serializedResultData,
                expected = null,
            )
            assertEquals(
                actual = it?.metadata?.lastErrorMessage,
                expected = "testError",
            )
        }
    }
}
