package com.copperleaf.ballast.scheduler.workmanager

import androidx.work.ListenableWorker
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkInfo
import com.copperleaf.ballast.scheduler.internal.RegisteredSchedule
import com.copperleaf.ballast.scheduler.schedule.dropHistory
import com.copperleaf.ballast.scheduler.workmanager.internal.getLongFromTag
import com.copperleaf.ballast.scheduler.workmanager.internal.getRegisteredSchedules
import com.copperleaf.ballast.scheduler.workmanager.internal.getStringFromTag
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.time.Duration

internal data class BallastWorkScheduleData(
    val workManagerData: BallastWorkManagerData,
    val registeredSchedule: RegisteredSchedule<*, *, *>,
    /**
     * The initial instant where this schedule was first created. Never changes.
     */
    val initialInstant: Instant,

    /**
     * The last time this schedule was fired, or the initial instant if it has not triggered yet.
     */
    val latestInstant: Instant,
) {
    public val key: String = registeredSchedule.key
    public val nextInstant: Instant? = if (workManagerData.withHistory) {
        registeredSchedule.schedule
            .dropHistory(initialInstant, latestInstant)
            .firstOrNull()
    } else {
        registeredSchedule.schedule
            .generateSchedule(latestInstant)
            .firstOrNull()
    }

    fun getDelayAmount(currentInstant: Instant): Duration {
        checkNotNull(nextInstant)
        return nextInstant - currentInstant
    }

    fun applyToWorkRequestBuilder(builder: OneTimeWorkRequest.Builder) {
        builder.addTag("$DATA_KEY$key")
        builder.addTag("$DATA_INITIAL_INSTANT${initialInstant.toEpochMilliseconds()}")
        builder.addTag("$DATA_LATEST_INSTANT${latestInstant.toEpochMilliseconds()}")
        builder.addTag("$DATA_NEXT_INSTANT${nextInstant?.toEpochMilliseconds()}")
    }

    override fun toString(): String {
        return "BallastWorkScheduleData(" +
                "registeredScheduleKey=$key, " +
                "initialInstant=$initialInstant, " +
                "latestInstant=$latestInstant, " +
                "nextInstant=$nextInstant" +
                ")"
    }

    internal companion object {
        internal const val DATA_KEY: String = "ballast::DATA_KEY::"
        internal const val DATA_INITIAL_INSTANT: String = "ballast::DATA_INITIAL_INSTANT::"
        internal const val DATA_LATEST_INSTANT: String = "ballast::DATA_LATEST_INSTANT::"
        internal const val DATA_NEXT_INSTANT: String = "ballast::DATA_NEXT_INSTANT::"

        /**
         * When getting schedule data from a worker, we are doing the actual work. The [latestInstant] will be set to the
         * current clock time, thus scheduling the next invocation.
         */
        suspend fun fromListenableWorker(
            workManagerData: BallastWorkManagerData,
            worker: ListenableWorker,
        ): BallastWorkScheduleData = with(worker) {
            val scheduleKey = getStringFromTag(DATA_KEY)
            val registeredSchedule: RegisteredSchedule<*, *, *> = workManagerData
                .adapter
                .getRegisteredSchedules()
                .single { it.key == scheduleKey }

            return BallastWorkScheduleData(
                workManagerData = workManagerData,
                registeredSchedule = registeredSchedule,
                initialInstant = Instant.fromEpochMilliseconds(getLongFromTag(DATA_INITIAL_INSTANT, 0)),
                latestInstant = Clock.System.now(),
            )
        }

        /**
         * When getting schedule data from a worker, we are updating the request with possibly new data. the
         * [latestInstant] will be kept at the same value it was initially started with, so that it won't reset or skip
         * the next invocation time.
         */
        suspend fun fromWorkInfo(
            workManagerData: BallastWorkManagerData,
            workInfo: WorkInfo,
        ): BallastWorkScheduleData = with(workInfo) {
            val scheduleKey = getStringFromTag(DATA_KEY)
            val registeredSchedule: RegisteredSchedule<*, *, *> = workManagerData
                .adapter
                .getRegisteredSchedules()
                .single { it.key == scheduleKey }

            return BallastWorkScheduleData(
                workManagerData = workManagerData,
                registeredSchedule = registeredSchedule,
                initialInstant = Instant.fromEpochMilliseconds(getLongFromTag(DATA_INITIAL_INSTANT, 0)),
                latestInstant = Instant.fromEpochMilliseconds(getLongFromTag(DATA_LATEST_INSTANT, 0)),
            )
        }
    }
}
