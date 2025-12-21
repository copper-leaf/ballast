package com.copperleaf.ballast.scheduler.operators

import com.copperleaf.ballast.scheduler.Schedule
import kotlin.time.Clock
import kotlin.time.Instant

/**
 * Transform the [Schedule] to only emit the next [n] values (or fewer if the upstream schedule terminates).
 */
public fun Schedule.take(n: Int): Schedule {
    return transformSchedule { scheduleSequence ->
        scheduleSequence.take(n)
    }
}

// Get values from a schedule
// ---------------------------------------------------------------------------------------------------------------------

/**
 * Using the provided [clock], get the schedule's nearest instant later than `clock.now()`
 */
public fun Schedule.getNext(clock: Clock = Clock.System): Instant? {
    return this.getNext(clock.now())
}

/**
 * Using a specified start Instant, get the schedule's nearest instant later than `clock.now()`
 */
public fun Schedule.getNext(instant: Instant): Instant? {
    return this.generateSchedule(instant).firstOrNull()
}

/**
 * Using a specified start Instant, get the schedule's nearest instant later than `clock.now()`
 */
public fun Schedule.getHistory(startInstant: Instant, currentInstant: Instant): Sequence<Instant> {
    return this.generateSchedule(startInstant)
        .takeWhile { it < currentInstant }
}

/**
 * Using a specified start Instant, get the schedule's nearest instant later than `clock.now()`
 */
public fun Schedule.dropHistory(startInstant: Instant, currentInstant: Instant): Sequence<Instant> {
    return this.generateSchedule(startInstant)
        .filter { it > currentInstant }
}
