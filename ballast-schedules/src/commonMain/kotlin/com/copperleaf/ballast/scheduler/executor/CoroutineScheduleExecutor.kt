package com.copperleaf.ballast.scheduler.executor

import com.copperleaf.ballast.scheduler.schedule.Schedule
import kotlinx.coroutines.CompletableDeferred
import kotlinx.datetime.Instant

/**
 * A [ScheduleExecutor] which executes tasks in-process, suspending for as long the schedule is still active (which may
 * be infinite).
 */
public interface CoroutineScheduleExecutor : ScheduleExecutor {

    /**
     * Execute a [schedule] at the specified interval. The schedule will continue to run as long as the coroutine is
     * alive, and this method may suspend indefinitely if the schedule sequence is infinite.
     *
     * On each iteration of the schedule, [enqueueTask] will be called if [predicate] is true. In the case that the
     * scheduled task is allowed to process, the iteration will track the time to the next iteration according to the
     * [delayMode]. In [DelayMode.FireAndForget], the next iteration is considered from the moment that the [enqueueTask] is
     * called, and the deferred passed to [enqueueTask] will be null. In [DelayMode.Suspend], a deferred will be passed
     * to [enqueueTask] to indicate that it should wait for the entire block to finish processing, and then complete
     * the deferred.
     */
    public suspend fun runSchedule(
        schedule: Schedule,
        delayMode: ScheduleExecutor.DelayMode = ScheduleExecutor.DelayMode.FireAndForget,
        shouldHandleTask: suspend () -> Boolean = { true },
        onTaskDropped: (Instant) -> Unit = { },
        enqueueTask: (Instant, CompletableDeferred<Unit>?) -> Unit,
    )
}
