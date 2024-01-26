package com.copperleaf.ballast.scheduler

import com.copperleaf.ballast.scheduler.executor.ScheduleExecutor
import com.copperleaf.ballast.scheduler.schedule.Schedule

public interface SchedulerAdapterScope<Inputs : Any, Events : Any, State : Any> {

    /**
     * Send the input returned by [scheduledInput] to be processed, with a delay schedule set by [schedule].
     *
     * If [delayMode] is [DelayMode.FromStart], this schedule attempts to sent Inputs at the exact same delay rate,
     * regardless of how long it takes to complete the job. This may cause jobs which take a long time to run to be
     * executed very close together.
     *
     * If [delayMode] is [DelayMode.FromEnd], this schedule attempts to wait the specified delay between the completion
     * of one job and the start of the next. Over time, the specific time at which the job executes will slip later and
     * later because of the processing time, but it will ensure the jobs do not execute too quickly.
     *
     * A [key] must be provided which uniquely identifies this schedule. From within the InputHandler, you may access
     * the [SchedulerInterceptor] and request that this schedule be stopped or paused.
     */
    public fun <T : Inputs> onSchedule(
        key: String,
        schedule: Schedule,
        delayMode: ScheduleExecutor.DelayMode = ScheduleExecutor.DelayMode.FireAndForget,
        scheduledInput: () -> T,
    )
}
