package com.copperleaf.ballast.scheduler.workmanager

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.work.WorkManager
import com.copperleaf.ballast.ExperimentalBallastApi
import com.copperleaf.ballast.scheduler.SchedulerAdapter
import com.copperleaf.ballast.scheduler.internal.RegisteredSchedule
import com.copperleaf.ballast.scheduler.schedule.Schedule
import com.copperleaf.ballast.scheduler.schedule.take
import com.copperleaf.ballast.scheduler.workmanager.internal.getRegisteredSchedules
import com.copperleaf.ballast.scheduler.workmanager.internal.syncSchedulesOnStartupInternal
import com.copperleaf.ballast.scheduler.workmanager.internal.syncSchedulesPeriodicallyInternal
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.time.Duration

// public API to Ballast Scheduler WorkManager
// ---------------------------------------------------------------------------------------------------------------------

/**
 * Schedule BallastWorkManagerScheduleDispatcher to run immediately as a unique job, replacing any existing
 * jobs. The job is keyed off the [adapter]'s filly-qualified class name.
 *
 * BallastWorkManagerScheduleDispatcher will make sure schedules are configured for all the scheduled registered
 * in the adapter. If a schedule does not exist, it will create it. If a registered schedule is already part of
 * WorkManager, it will update it with the latest info so any changes to the schedule will be applied. If there are
 * enqueued jobs at keys which are not in the Adapter's registered schedules, those will be cancelled, as it will be
 * assumed they were configured in a previous release, but removed in the current version.
 *
 * Both the [adapter] and the [callback] will be created via reflection as the schedule is triggered, and must have a
 * no-argument constructor available.
 *
 * In some cases, you may need to run a schedule that triggers only a certain number of times. By default, the next
 * invocation of a schedule is calculated based on the current Instant at which the schedule is created or updated,
 * which has no knowledge of how many times the schedule has run already. By setting [withHistory] to true, the next
 * scheduled instant will be calculated from the point it was initially registered, with all intermediate values
 * computed until it finds one in the future, and schedules the next invocation to that Instant. This allows you to use
 * an operator like [Schedule.take] that depends on the invocation history, and now just the current Clock time.
 *
 * WorkManager schedules are computed with the [Clock.System] clock, and cannot use any custom-defined clocks.
 *
 * This method will sync the scheduled jobs only once. When called from your `Application.onCreate` or from a Startup,
 * it will sync schedules every time the app is opened. This is useful if all your schedules are hardcoded and would
 * only change with an app update.
 */
@ExperimentalBallastApi
@RequiresApi(Build.VERSION_CODES.O)
public fun <I : Any, E : Any, S : Any> WorkManager.syncSchedulesOnStartup(
    adapter: SchedulerAdapter<I, E, S>,
    callback: SchedulerCallback<I>,
    withHistory: Boolean,
) {
    syncSchedulesOnStartupInternal(adapter, callback, withHistory)
}

/**
 * Schedule BallastWorkManagerScheduleDispatcher to run immediately as a unique job, replacing any existing
 * jobs. The job is keyed off the [adapter]'s filly-qualified class name.
 *
 * BallastWorkManagerScheduleDispatcher will make sure schedules are configured for all the scheduled registered
 * in the adapter. If a schedule does not exist, it will create it. If a registered schedule is already part of
 * WorkManager, it will update it with the latest info so any changes to the schedule will be applied. If there are
 * enqueued jobs at keys which are not in the Adapter's registered schedules, those will be cancelled, as it will be
 * assumed they were configured in a previous release, but removed in the current version.
 *
 * Both the [adapter] and the [callback] will be created via reflection as the schedule is triggered, and must have a
 * no-argument constructor available.
 *
 * In some cases, you may need to run a schedule that triggers only a certain number of times. By default, the next
 * invocation of a schedule is calculated based on the current Instant at which the schedule is created or updated,
 * which has no knowledge of how many times the schedule has run already. By setting [withHistory] to true, the next
 * scheduled instant will be calculated from the point it was initially registered, with all intermediate values
 * computed until it finds one in the future, and schedules the next invocation to that Instant. This allows you to use
 * an operator like [Schedule.take] that depends on the invocation history, and now just the current Clock time.
 *
 * WorkManager schedules are computed with the [Clock.System] clock, and cannot use any custom-defined clocks.
 *
 * This method will sync the scheduled jobs on a periodic schedule. This is useful if the scheduled jobs are loaded
 * dynamically and need to be updated without an app update or user-intervention (such as user-generated calendar
 * event notifications).
 */
@ExperimentalBallastApi
@RequiresApi(Build.VERSION_CODES.O)
public fun <I : Any, E : Any, S : Any> WorkManager.syncSchedulesPeriodically(
    adapter: SchedulerAdapter<I, E, S>,
    callback: SchedulerCallback<I>,
    withHistory: Boolean,
    period: Duration,
) {
    syncSchedulesPeriodicallyInternal(adapter, callback, withHistory, period)
}

/**
 * FOR DEBUGGING PURPOSES ONLY!
 *
 * Test the delivery of a scheduled job now, identified by its adapter and schedule name. It does not actually run a
 */
@Suppress("UNCHECKED_CAST")
@RequiresApi(Build.VERSION_CODES.O)
public suspend fun testScheduleNow(
    applicationContext: Context,
    adapterClassName: String,
    callbackClassName: String,
    scheduleKey: String,
    initialInstant: Instant = Clock.System.now(),
    latestInstant: Instant = Clock.System.now(),
    withHistory: Boolean = false,
) {
    // unpack data from the work input data, and create adapter and callback classes with reflection
    val workManager = WorkManager.getInstance(applicationContext)

    val workManagerData = BallastWorkManagerData(
        adapter = (Class.forName(adapterClassName) as Class<SchedulerAdapter<*, *, *>>)
            .getConstructor()
            .newInstance(),
        callback = (Class.forName(callbackClassName) as Class<SchedulerCallback<*>>)
            .getConstructor()
            .newInstance(),
        withHistory = withHistory,
    )
    val registeredSchedule: RegisteredSchedule<*, *, *> = workManagerData
        .adapter
        .getRegisteredSchedules()
        .single { it.key == scheduleKey }

    val scheduleData = BallastWorkScheduleData(
        workManagerData = workManagerData,
        registeredSchedule = registeredSchedule,
        initialInstant = initialInstant,
        latestInstant = latestInstant,
    )

    BallastWorkManagerScheduleWorker.dispatchWork(workManager, scheduleData)
}
