package com.copperleaf.ballast.scheduler.executor

import com.copperleaf.ballast.scheduler.schedule.Schedule
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.delay
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

public class CoroutineClockScheduleExecutor(
    private val clock: Clock = Clock.System,
) : CoroutineScheduleExecutor {

    override suspend fun runSchedule(
        schedule: Schedule,
        delayMode: ScheduleExecutor.DelayMode,
        shouldHandleTask: suspend () -> Boolean,
        onTaskDropped: (Instant) -> Unit,
        enqueueTask: (Instant, CompletableDeferred<Unit>?) -> Unit,
    ) {
        schedule
            .generateSchedule(clock.now())
            .forEach { nextScheduleInstant ->
                val currentInstant = clock.now()

                if (nextScheduleInstant >= currentInstant) {
                    handleScheduledDelay(nextScheduleInstant)
                    handleScheduledEvent(nextScheduleInstant, delayMode, shouldHandleTask, enqueueTask)
                } else {
                    onTaskDropped(nextScheduleInstant)
                }
            }
    }

    private suspend fun handleScheduledDelay(
        nextScheduleInstant: Instant,
    ) {
        // wait the appropriate amount of time until we hit the next scheduled instant
        val currentInstant = clock.now()
        val delayDuration = nextScheduleInstant - currentInstant
        delay(delayDuration)
    }

    private suspend fun handleScheduledEvent(
        nextScheduleInstant: Instant,
        delayMode: ScheduleExecutor.DelayMode,
        shouldHandleTask: suspend () -> Boolean,
        enqueueTask: (Instant, CompletableDeferred<Unit>?) -> Unit,
    ) {
        if (shouldHandleTask()) {
            when (delayMode) {
                ScheduleExecutor.DelayMode.FireAndForget -> {
                    // if measuring the delay from start, we don't need to track when the Input
                    // completes. This helps keep the Inputs sent at a fixed interval, regardless of how
                    // long it takes to process the job.
                    //
                    // However, if the processing time is close to the delay time, it could mean that
                    // there's very little delay between subsequent runs.
                    enqueueTask(nextScheduleInstant, null)
                }

                ScheduleExecutor.DelayMode.Suspend -> {
                    // if measuring from end, we need to wait for the Input to complete, before
                    // scheduling the next real delay.
                    //
                    // This ensures that a minimum amount of time is spent between jobs, but the
                    // specific time when the job runs is going to slip later and later over time.
                    val deferred = CompletableDeferred<Unit>()
                    enqueueTask(nextScheduleInstant, deferred)
                    deferred.await()
                }
            }
        }
    }
}
