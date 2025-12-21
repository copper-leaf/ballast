package com.copperleaf.ballast.scheduler.operators

import com.copperleaf.ballast.scheduler.Schedule
import kotlin.time.Duration
import kotlin.time.Instant

/**
 * Delay the first emission of a Schedule by a fixed [delay].
 */
public fun Schedule.delayed(delay: Duration): Schedule {
    return transformScheduleStart { start ->
        start + delay
    }
}

/**
 * Delay the first emission of a Schedule until a specific [startInstant]. If the schedule was started with an Instant
 * that is later than [startInstant], that later Instant will be used instead, since it is still after [startInstant].
 */
public fun Schedule.delayedUntil(startInstant: Instant): Schedule {
    return transformScheduleStart { start ->
        maxOf(start, startInstant)
    }
}
