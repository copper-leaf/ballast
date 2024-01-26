package com.copperleaf.ballast.scheduler.schedule

import com.copperleaf.ballast.scheduler.executor.CoroutineClockScheduleExecutor
import com.copperleaf.ballast.scheduler.executor.ScheduleExecutor
import io.kotest.core.spec.style.FunSpec
import io.kotest.core.test.testCoroutineScheduler
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Duration.Companion.minutes

@OptIn(ExperimentalStdlibApi::class, ExperimentalCoroutinesApi::class)
class FixedClockScheduleExecutorTest : FunSpec({
    val timeZone = TimeZone.UTC

    // with fromStart, tasks are always dispatched on their idea schedule
    test("from start executor test").config(coroutineTestScope = true) {
        // skip tests on JS because TestDispatcher is not set up properly https://github.com/kotest/kotest/issues/3575
        if (coroutineContext[CoroutineDispatcher] !is TestDispatcher) return@config

        val testClock = object : Clock {
            override fun now(): Instant {
                return Instant.fromEpochMilliseconds(testCoroutineScheduler.currentTime)
            }
        }
        val executor = CoroutineClockScheduleExecutor(testClock)
        val schedule = FixedDelaySchedule(10.minutes)
            .take(4)
        val droppedTasks = mutableListOf<LocalDateTime>()
        val acceptedTasks = mutableListOf<LocalDateTime>()

        launch {
            executor.runSchedule(
                schedule = schedule,
                delayMode = ScheduleExecutor.DelayMode.FireAndForget,
                shouldHandleTask = { true },
                onTaskDropped = { scheduledInstant -> droppedTasks += scheduledInstant.toLocalDateTime(timeZone) },
                enqueueTask = { scheduledInstant, _ -> acceptedTasks += scheduledInstant.toLocalDateTime(timeZone) },
            )
        }

        testCoroutineScheduler.advanceUntilIdle()

        acceptedTasks shouldBe listOf(
            LocalDate(1970, Month.JANUARY, 1).atTime(0, 10),
            LocalDate(1970, Month.JANUARY, 1).atTime(0, 20),
            LocalDate(1970, Month.JANUARY, 1).atTime(0, 30),
            LocalDate(1970, Month.JANUARY, 1).atTime(0, 40),
        )
        droppedTasks.shouldBeEmpty()
    }

    // with fast jobs, the schedule is able to keep to its intended ideal schedule
    test("from end with fast jobs executor test").config(coroutineTestScope = true) {
        // skip tests on JS because TestDispatcher is not set up properly https://github.com/kotest/kotest/issues/3575
        if (coroutineContext[CoroutineDispatcher] !is TestDispatcher) return@config

        val testClock = object : Clock {
            override fun now(): Instant {
                return Instant.fromEpochMilliseconds(testCoroutineScheduler.currentTime)
            }
        }
        val executor = CoroutineClockScheduleExecutor(testClock)
        val schedule = FixedDelaySchedule(10.minutes)
            .take(4)
        val droppedTasks = mutableListOf<LocalDateTime>()
        val acceptedTasks = mutableListOf<LocalDateTime>()

        launch {
            val scope = this

            executor.runSchedule(
                schedule = schedule,
                delayMode = ScheduleExecutor.DelayMode.Suspend,
                shouldHandleTask = { true },
                onTaskDropped = { scheduledInstant -> droppedTasks += scheduledInstant.toLocalDateTime(timeZone) },
                enqueueTask = { scheduledInstant, deferred ->
                    acceptedTasks += scheduledInstant.toLocalDateTime(timeZone)

                    scope.launch {
                        delay(5.minutes)
                        deferred!!.complete(Unit)
                    }
                },
            )
        }

        testCoroutineScheduler.advanceUntilIdle()

        acceptedTasks shouldBe listOf(
            LocalDate(1970, Month.JANUARY, 1).atTime(0, 10),
            LocalDate(1970, Month.JANUARY, 1).atTime(0, 20),
            LocalDate(1970, Month.JANUARY, 1).atTime(0, 30),
            LocalDate(1970, Month.JANUARY, 1).atTime(0, 40),
        )
        droppedTasks.shouldBeEmpty()
    }

    // with slow jobs that take longer than the schedule period, some tasks will be dropped
    test("from end with slow jobs executor test").config(coroutineTestScope = true) {
        // skip tests on JS because TestDispatcher is not set up properly https://github.com/kotest/kotest/issues/3575
        if (coroutineContext[CoroutineDispatcher] !is TestDispatcher) return@config

        val testClock = object : Clock {
            override fun now(): Instant {
                return Instant.fromEpochMilliseconds(testCoroutineScheduler.currentTime)
            }
        }
        val executor = CoroutineClockScheduleExecutor(testClock)
        val schedule = FixedDelaySchedule(10.minutes)
            .take(4)
        val droppedTasks = mutableListOf<LocalDateTime>()
        val acceptedTasks = mutableListOf<LocalDateTime>()

        launch {
            val scope = this

            executor.runSchedule(
                schedule = schedule,
                delayMode = ScheduleExecutor.DelayMode.Suspend,
                shouldHandleTask = { true },
                onTaskDropped = { scheduledInstant -> droppedTasks += scheduledInstant.toLocalDateTime(timeZone) },
                enqueueTask = { scheduledInstant, deferred ->
                    acceptedTasks += scheduledInstant.toLocalDateTime(timeZone)

                    scope.launch {
                        delay(15.minutes)
                        deferred!!.complete(Unit)
                    }
                },
            )
        }

        testCoroutineScheduler.advanceUntilIdle()

        acceptedTasks shouldBe listOf(
            LocalDate(1970, Month.JANUARY, 1).atTime(0, 10),
            LocalDate(1970, Month.JANUARY, 1).atTime(0, 30),
        )
        droppedTasks shouldBe listOf(
            LocalDate(1970, Month.JANUARY, 1).atTime(0, 20),
            LocalDate(1970, Month.JANUARY, 1).atTime(0, 40),
        )
    }
})
