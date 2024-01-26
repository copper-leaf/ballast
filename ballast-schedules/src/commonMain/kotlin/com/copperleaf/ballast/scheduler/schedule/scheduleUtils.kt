package com.copperleaf.ballast.scheduler.schedule

import kotlinx.datetime.Clock
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Duration

// Basic schedule transformation functions
// ---------------------------------------------------------------------------------------------------------------------

public inline fun Schedule.transformSchedule(crossinline block: (Sequence<Instant>) -> Sequence<Instant>): Schedule {
    val scheduleDelegate = this
    return Schedule { start ->
        scheduleDelegate
            .generateSchedule(start)
            .let(block)
    }
}

public inline fun Schedule.transformScheduleStart(crossinline block: (Instant) -> Instant): Schedule {
    val scheduleDelegate = this
    return Schedule { start ->
        scheduleDelegate
            .generateSchedule(block(start))
    }
}

// Configure a schedule with additional behavior
// ---------------------------------------------------------------------------------------------------------------------

/**
 * For a [FixedDelaySchedule], make the subsequent items delayed by the amount of time it takes to process them, rather
 * than always generating a fixed interval. THis adapts the sequence such that there if a fixed amount of time between
 * the end of one task and the start of another.
 */
public fun Schedule.adaptive(clock: Clock = Clock.System): Schedule {
    return transformSchedule { scheduleSequence ->
        sequence {
            val iterator = scheduleSequence.iterator()
            var current = iterator.next()

            yield(current)

            while (iterator.hasNext()) {
                val next = iterator.next()
                val intendedDelay = current - next
                val now = clock.now()
                val actualDelayedInstant = now - intendedDelay
                yield(actualDelayedInstant)

                current = next
            }
        }
    }
}

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

/**
 * Only process scheduled tasks which are within the bounds (inclusive) of the [validRange]. Instants emitted before the
 * start of the range will be ignored, and the first Instant emitted after the end of the range will terminate the
 * sequence, making it finite.
 */
public fun Schedule.bounded(validRange: ClosedRange<Instant>): Schedule {
    check(!validRange.isEmpty()) {
        "the valid range of dates cannot be empty"
    }

    return transformSchedule { scheduleSequence ->
        sequence {
            val iterator = scheduleSequence.iterator()

            while (iterator.hasNext()) {
                val next = iterator.next()

                println("checking $next")

                when {
                    next < validRange.start -> {
                        // we haven't entered the start of the range, don't quit yet
                        continue
                    }

                    next in validRange -> {
                        // we are withing the valid range, yield the values downstream
                        yield(next)
                    }

                    next > validRange.endInclusive -> {
                        // we are past the end of the range, quit the loop
                        break
                    }

                    else -> {
                        // not possible
                        break
                    }
                }
            }
        }
    }
}

public fun Schedule.until(endInclusive: Instant): Schedule {
    return transformSchedule { scheduleSequence ->
        scheduleSequence.takeWhile { it <= endInclusive }
    }
}

public fun Schedule.take(n: Int): Schedule {
    return transformSchedule { scheduleSequence ->
        scheduleSequence.take(n)
    }
}

public fun Schedule.filterByDayOfWeek(vararg daysOfWeek: DayOfWeek, timeZone: TimeZone = TimeZone.UTC): Schedule {
    return transformSchedule { scheduleSequence ->
        scheduleSequence
            .filter {
                val localDateTime = it.toLocalDateTime(timeZone)
                localDateTime.dayOfWeek in daysOfWeek
            }
    }
}

public fun Schedule.weekdays(timeZone: TimeZone = TimeZone.UTC): Schedule {
    return filterByDayOfWeek(
        DayOfWeek.MONDAY,
        DayOfWeek.TUESDAY,
        DayOfWeek.WEDNESDAY,
        DayOfWeek.THURSDAY,
        DayOfWeek.FRIDAY,
        timeZone = timeZone,
    )
}

public fun Schedule.weekends(timeZone: TimeZone = TimeZone.UTC): Schedule {
    return filterByDayOfWeek(
        DayOfWeek.SUNDAY,
        DayOfWeek.SATURDAY,
        timeZone = timeZone,
    )
}

// Get values from a schedule
// ---------------------------------------------------------------------------------------------------------------------

/**
 * Using the default system Clock, get the schedule's nearest instant later than `clock.now()`
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
