package com.copperleaf.ballast.queue.executor

import com.copperleaf.ballast.queue.JobStatus
import com.copperleaf.ballast.queue.QueueExecutor
import com.copperleaf.ballast.queue.QueueExecutorScope
import com.copperleaf.ballast.queue.SerializedJob
import com.copperleaf.ballast.queue.driver.InMemoryQueueDriver
import com.copperleaf.ballast.scheduler.TestClock
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.asTimeSource
import kotlinx.datetime.atStartOfDayIn
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

@Suppress("DEPRECATION")
@OptIn(ExperimentalCoroutinesApi::class)
class DefaultQueueExecutorTest {

    @Serializable
    data class TestPayload(val data: String)

    @Serializable
    data class TestState(val step: Int = 0)

    @Serializable
    data class TestResult(val resultData: String)

    private class TestAdapter(
        private val clock: Clock,
    ) : QueueExecutor.Adapter<InMemoryQueueDriver.Metadata, TestPayload, TestResult, TestState> {
        override fun getJobTimeout(payload: TestPayload): Duration {
            return 30.seconds
        }

        override fun getJobMetadata(payload: TestPayload): InMemoryQueueDriver.Metadata {
            return InMemoryQueueDriver.Metadata(
                insertedAt = clock.now(),
                maxAttempts = 5,
            )
        }

        override fun getDefaultRetryDelayTimeout(payload: TestPayload): Duration {
            return 60.seconds
        }
    }

    val serializers = JsonSerializers(TestPayload.serializer(), TestResult.serializer(), TestState.serializer())

    @Test
    fun insertJob() = runTest {
        val timezone = TimeZone.Companion.UTC
        val startInstant = LocalDate(2025, 1, 1).atStartOfDayIn(timezone)
        val clock = TestClock(startInstant)
        val driver = InMemoryQueueDriver(clock)
        val executor = DefaultQueueExecutor(
            driver = driver,
            adapter = TestAdapter(clock),
            serializers = serializers,
            captureErrorStacktrace = false,
            timeSource = clock.asTimeSource(),
        )

        val uuid = executor.insertJob("one", TestPayload("ballast"))

        assertEquals(
            actual = driver.observeJobState(uuid).firstOrNull(),
            expected = SerializedJob(
                jobId = uuid,
                queueName = "one",
                serializedPayload = buildJsonObject { put("data", "ballast") }.toString(),
                timeoutDuration = 30.seconds,
                serializedState = "{}",
                status = JobStatus.Pending,
                serializedResultData = null,
                metadata = InMemoryQueueDriver.Metadata(
                    insertedAt = startInstant,
                    priority = 0,
                    runAt = startInstant,
                    maxAttempts = 5,
                    attempts = 0,
                    lastRunDuration = null,
                ),
            ),
        )
    }

    @Test
    fun processing_success() = runTest {
        val timezone = TimeZone.Companion.UTC
        val startInstant = LocalDate(2025, 1, 1).atStartOfDayIn(timezone)
        val clock = TestClock(startInstant)
        val driver = InMemoryQueueDriver(clock)
        val executor = DefaultQueueExecutor(
            driver = driver,
            adapter = TestAdapter(clock),
            serializers = serializers,
            captureErrorStacktrace = false,
            timeSource = clock.asTimeSource(),
        )

        val uuid = executor.insertJob("one", TestPayload("ballast"))

        executor
            .runQueue("one") { payload -> TestResult(payload.data.uppercase()) }
            .first()

        val jobState = driver.observeJobState(uuid).firstOrNull()

        assertEquals(
            actual = jobState,
            expected = SerializedJob(
                jobId = uuid,
                queueName = "one",
                serializedPayload = buildJsonObject { put("data", "ballast") }.toString(),
                timeoutDuration = 30.seconds,
                serializedState = "{}",
                status = JobStatus.Completed,
                serializedResultData = buildJsonObject {
                    put("resultData", "BALLAST")
                }.toString(),
                metadata = InMemoryQueueDriver.Metadata(
                    insertedAt = startInstant,
                    priority = 0,
                    runAt = startInstant,
                    maxAttempts = 5,
                    attempts = 1,
                    lastRunDuration = Duration.Companion.ZERO,
                ),
            ),
        )
    }

    @Test
    fun processing_cancellation() = runTest {
        val timezone = TimeZone.Companion.UTC
        val startInstant = LocalDate(2025, 1, 1).atStartOfDayIn(timezone)
        val clock = TestClock(startInstant)
        val driver = InMemoryQueueDriver(clock)
        val executor = DefaultQueueExecutor(
            driver = driver,
            adapter = TestAdapter(clock),
            serializers = serializers,
            captureErrorStacktrace = false,
            timeSource = clock.asTimeSource(),
        )

        val uuid = executor.insertJob("one", TestPayload("ballast"))

        launch {
            delay(10.seconds)
            driver.requestJobCancellation(uuid)
        }

        executor
            .runQueue("one") { payload ->
                delay(20.seconds)
                TestResult(payload.data.uppercase())
            }
            .first()

        val jobState = driver.observeJobState(uuid).firstOrNull()

        assertEquals(
            actual = jobState,
            expected = SerializedJob(
                jobId = uuid,
                queueName = "one",
                serializedPayload = buildJsonObject { put("data", "ballast") }.toString(),
                timeoutDuration = 30.seconds,
                serializedState = "{}",
                status = JobStatus.Pending,
                serializedResultData = buildJsonObject {
                    put("reason", "cancelled")
                }.toString(),
                metadata = InMemoryQueueDriver.Metadata(
                    insertedAt = startInstant,
                    priority = 0,
                    runAt = startInstant + 70.seconds, // time until cancellation + retry delay
                    maxAttempts = 5,
                    attempts = 1,
                    lastRunDuration = 10.seconds,
                ),
            ),
        )
    }

    @Test
    fun processing_timeout() = runTest {
        val timezone = TimeZone.Companion.UTC
        val startInstant = LocalDate(2025, 1, 1).atStartOfDayIn(timezone)
        val clock = TestClock(startInstant)
        val driver = InMemoryQueueDriver(clock)
        val executor = DefaultQueueExecutor(
            driver = driver,
            adapter = TestAdapter(clock),
            serializers = serializers,
            captureErrorStacktrace = false,
            timeSource = clock.asTimeSource(),
        )

        val uuid = executor.insertJob("one", TestPayload("ballast"))

        executor
            .runQueue("one") { payload ->
                delay(1.minutes)
                TestResult(payload.data.uppercase())
            }
            .first()

        val jobState = driver.observeJobState(uuid).firstOrNull()

        assertEquals(
            actual = jobState,
            expected = SerializedJob(
                jobId = uuid,
                queueName = "one",
                serializedPayload = buildJsonObject { put("data", "ballast") }.toString(),
                timeoutDuration = 30.seconds,
                serializedState = "{}",
                status = JobStatus.Pending,
                serializedResultData = buildJsonObject {
                    put(
                        "error",
                        "Timed out after 30s of _virtual_ (kotlinx.coroutines.test) time. To use the real time, wrap 'withTimeout' in 'withContext(Dispatchers.Default.limitedParallelism(1))'"
                    )
                    put("reason", "timeout")
                }.toString(),
                metadata = InMemoryQueueDriver.Metadata(
                    insertedAt = startInstant,
                    priority = 0,
                    runAt = startInstant + 90.seconds, // the time for the timeout + retry delay
                    maxAttempts = 5,
                    attempts = 1,
                    lastRunDuration = 30.seconds,
                ),
            ),
        )
    }

    @Test
    fun processing_normalFailure() = runTest {
        val timezone = TimeZone.Companion.UTC
        val startInstant = LocalDate(2025, 1, 1).atStartOfDayIn(timezone)
        val clock = TestClock(startInstant)
        val driver = InMemoryQueueDriver(clock)
        val executor = DefaultQueueExecutor(
            driver = driver,
            adapter = TestAdapter(clock),
            serializers = serializers,
            captureErrorStacktrace = false,
            timeSource = clock.asTimeSource(),
        )

        val uuid = executor.insertJob("one", TestPayload("ballast"))

        executor
            .runQueue("one") { payload ->
                throw JobFailureException(RuntimeException("normal error"), 45.seconds)
            }
            .first()

        val jobState = driver.observeJobState(uuid).firstOrNull()

        assertEquals(
            actual = jobState,
            expected = SerializedJob(
                jobId = uuid,
                queueName = "one",
                serializedPayload = buildJsonObject { put("data", "ballast") }.toString(),
                timeoutDuration = 30.seconds,
                serializedState = "{}",
                status = JobStatus.Pending,
                serializedResultData = buildJsonObject {
                    put("error", "normal error")
                    put("reason", "exception")
                }.toString(),
                metadata = InMemoryQueueDriver.Metadata(
                    insertedAt = startInstant,
                    priority = 0,
                    runAt = startInstant + 45.seconds,
                    maxAttempts = 5,
                    attempts = 1,
                    lastRunDuration = Duration.Companion.ZERO,
                ),
            ),
        )
    }

    @Test
    fun processing_abnormalFailure() = runTest {
        val timezone = TimeZone.Companion.UTC
        val startInstant = LocalDate(2025, 1, 1).atStartOfDayIn(timezone)
        val clock = TestClock(startInstant)
        val driver = InMemoryQueueDriver(clock)
        val executor = DefaultQueueExecutor(
            driver = driver,
            adapter = TestAdapter(clock),
            serializers = serializers,
            captureErrorStacktrace = false,
            timeSource = clock.asTimeSource(),
        )

        val uuid = executor.insertJob("one", TestPayload("ballast"))

        executor
            .runQueue("one") { payload ->
                throw RuntimeException("normal error")
            }
            .first()

        val jobState = driver.observeJobState(uuid).firstOrNull()

        assertEquals(
            actual = jobState,
            expected = SerializedJob(
                jobId = uuid,
                queueName = "one",
                serializedPayload = buildJsonObject { put("data", "ballast") }.toString(),
                timeoutDuration = 30.seconds,
                serializedState = "{}",
                status = JobStatus.Pending,
                serializedResultData = buildJsonObject {
                    put("error", "normal error")
                    put("reason", "exception")
                }.toString(),
                metadata = InMemoryQueueDriver.Metadata(
                    insertedAt = startInstant,
                    priority = 0,
                    runAt = startInstant + 60.seconds,
                    maxAttempts = 5,
                    attempts = 1,
                    lastRunDuration = Duration.Companion.ZERO,
                ),
            ),
        )
    }

    @Test
    fun processing_intermediateState() = runTest {
        val timezone = TimeZone.Companion.UTC
        val startInstant = LocalDate(2025, 1, 1).atStartOfDayIn(timezone)
        val clock = TestClock(startInstant)
        val driver = InMemoryQueueDriver(clock)
        val executor = DefaultQueueExecutor(
            driver = driver,
            adapter = TestAdapter(clock),
            serializers = serializers,
            captureErrorStacktrace = false,
            timeSource = clock.asTimeSource(),
        )

        val uuid = executor.insertJob("one", TestPayload("ballast"))

        val processor: suspend QueueExecutorScope<TestState>.(TestPayload) -> TestResult? = { payload ->
            val state = getCurrentState()

            if (state.step == 0) {
                delay(5.seconds)
                setState(state.copy(step = state.step + 1))
                throw RuntimeException("please try again")
            }
            if (state.step == 1) {
                delay(5.seconds)
                setState(state.copy(step = state.step + 1))
                throw RuntimeException("please try again")
            }
            if (state.step == 2) {
                delay(5.seconds)
                setState(state.copy(step = state.step + 1))
                throw RuntimeException("please try again")
            }
            if (state.step == 3) {
                delay(5.seconds)
                setState(state.copy(step = state.step + 1))
            }

            TestResult(payload.data.uppercase())
        }

        // process first attempt
        executor
            .runQueue("one", processor)
            .first()

        assertEquals(
            actual = driver.observeJobState(uuid).firstOrNull(),
            expected = SerializedJob(
                jobId = uuid,
                queueName = "one",
                serializedPayload = buildJsonObject { put("data", "ballast") }.toString(),
                timeoutDuration = 30.seconds,
                serializedState = buildJsonObject {
                    put("step", 1)
                }.toString(),
                status = JobStatus.Pending,
                serializedResultData = buildJsonObject {
                    put("error", "please try again")
                    put("reason", "exception")
                }.toString(),
                metadata = InMemoryQueueDriver.Metadata(
                    insertedAt = startInstant,
                    priority = 0,
                    runAt = startInstant + 65.seconds,
                    maxAttempts = 5,
                    attempts = 1,
                    lastRunDuration = 5.seconds,
                ),
            ),
        )

        // process second attempt
        executor
            .runQueue("one", processor)
            .first()

        assertEquals(
            actual = driver.observeJobState(uuid).firstOrNull(),
            expected = SerializedJob(
                jobId = uuid,
                queueName = "one",
                serializedPayload = buildJsonObject { put("data", "ballast") }.toString(),
                timeoutDuration = 30.seconds,
                serializedState = buildJsonObject {
                    put("step", 2)
                }.toString(),
                status = JobStatus.Pending,
                serializedResultData = buildJsonObject {
                    put("error", "please try again")
                    put("reason", "exception")
                }.toString(),
                metadata = InMemoryQueueDriver.Metadata(
                    insertedAt = startInstant,
                    priority = 0,
                    runAt = startInstant + (65.seconds * 2),
                    maxAttempts = 5,
                    attempts = 2,
                    lastRunDuration = 5.seconds,
                ),
            ),
        )

        // process second attempt
        executor
            .runQueue("one", processor)
            .first()

        assertEquals(
            actual = driver.observeJobState(uuid).firstOrNull(),
            expected = SerializedJob(
                jobId = uuid,
                queueName = "one",
                serializedPayload = buildJsonObject { put("data", "ballast") }.toString(),
                timeoutDuration = 30.seconds,
                serializedState = buildJsonObject {
                    put("step", 3)
                }.toString(),
                status = JobStatus.Pending,
                serializedResultData = buildJsonObject {
                    put("error", "please try again")
                    put("reason", "exception")
                }.toString(),
                metadata = InMemoryQueueDriver.Metadata(
                    insertedAt = startInstant,
                    priority = 0,
                    runAt = startInstant + (65.seconds * 3),
                    maxAttempts = 5,
                    attempts = 3,
                    lastRunDuration = 5.seconds,
                ),
            ),
        )

        // process second attempt
        executor
            .runQueue("one", processor)
            .first()

        assertEquals(
            actual = driver.observeJobState(uuid).firstOrNull(),
            expected = SerializedJob(
                jobId = uuid,
                queueName = "one",
                serializedPayload = buildJsonObject { put("data", "ballast") }.toString(),
                timeoutDuration = 30.seconds,
                serializedState = buildJsonObject {
                    put("step", 4)
                }.toString(),
                status = JobStatus.Completed,
                serializedResultData = buildJsonObject {
                    put("resultData", "BALLAST")
                }.toString(),
                metadata = InMemoryQueueDriver.Metadata(
                    insertedAt = startInstant,
                    priority = 0,
                    runAt = startInstant + (65.seconds * 3),
                    maxAttempts = 5,
                    attempts = 4,
                    lastRunDuration = 5.seconds,
                ),
            ),
        )
    }
}
