package com.copperleaf.ballast.scheduler.schedule

import com.copperleaf.ballast.scheduler.executor.CoroutineClockScheduleExecutor
import com.copperleaf.ballast.scheduler.executor.ScheduleExecutor
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlinx.datetime.toLocalDateTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.minutes

@OptIn(ExperimentalCoroutinesApi::class)
class AdaptiveClockScheduleExecutorTest {
    val timeZone = TimeZone.UTC

    // with fromStart, tasks are always dispatched on their idea schedule
    @Test
    fun fromStartExecutorTest() = runTest {
        val testClock = object : Clock {
            override fun now(): Instant {
                return Instant.fromEpochMilliseconds(currentTime)
            }
        }
        val executor = CoroutineClockScheduleExecutor(testClock)
        val schedule = FixedDelaySchedule(10.minutes)
            .adaptive(testClock)
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

        advanceUntilIdle()

        assertEquals<Any?>(
            listOf(
                LocalDate(1970, Month.JANUARY, 1).atTime(0, 10),
                LocalDate(1970, Month.JANUARY, 1).atTime(0, 20),
                LocalDate(1970, Month.JANUARY, 1).atTime(0, 30),
                LocalDate(1970, Month.JANUARY, 1).atTime(0, 40),
            ), acceptedTasks
        )
        assertEquals(0, droppedTasks.size)
    }

    // with fast jobs, the schedule is able to keep to its intended ideal schedule
    @Test
    fun fromEndWithFastJobsExecutorTest() = runTest {
        val testClock = object : Clock {
            override fun now(): Instant {
                return Instant.fromEpochMilliseconds(currentTime)
            }
        }
        val executor = CoroutineClockScheduleExecutor(testClock)
        val schedule = FixedDelaySchedule(10.minutes)
            .adaptive(testClock)
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

        advanceUntilIdle()

        assertEquals<Any?>(
            listOf(
                LocalDate(1970, Month.JANUARY, 1).atTime(0, 10),
                LocalDate(1970, Month.JANUARY, 1).atTime(0, 25),
                LocalDate(1970, Month.JANUARY, 1).atTime(0, 40),
                LocalDate(1970, Month.JANUARY, 1).atTime(0, 55),
            ), acceptedTasks
        )
        assertEquals(0, droppedTasks.size)
    }

    // with slow jobs that take longer than the schedule period, some tasks will be dropped
    @Test
    fun fromEndWithSlowJobsExecutorTest() = runTest {
        val testClock = object : Clock {
            override fun now(): Instant {
                return Instant.fromEpochMilliseconds(currentTime)
            }
        }
        val executor = CoroutineClockScheduleExecutor(testClock)
        val schedule = FixedDelaySchedule(10.minutes)
            .adaptive(testClock)
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

        advanceUntilIdle()

        assertEquals<Any?>(
            listOf(
                LocalDate(1970, Month.JANUARY, 1).atTime(0, 10),
                LocalDate(1970, Month.JANUARY, 1).atTime(0, 35),
                LocalDate(1970, Month.JANUARY, 1).atTime(1, 0),
                LocalDate(1970, Month.JANUARY, 1).atTime(1, 25),
            ), acceptedTasks
        )
        assertEquals(0, droppedTasks.size)
    }
}
