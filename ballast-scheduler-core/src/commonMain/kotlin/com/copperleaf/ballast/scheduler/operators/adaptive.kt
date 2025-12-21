package com.copperleaf.ballast.scheduler.operators

import com.copperleaf.ballast.scheduler.schedule.Schedule
import kotlin.time.Clock

/**
 * Transform a Schedule to be adaptive, meaning that it will adjust its timing based on the actual time taken to process
 * each item.
 *
 * make the subsequent items delayed by the amount of time it takes to process them, rather
 * than always generating a fixed interval. THis adapts the sequence such that there if a fixed amount of time between
 * the end of one task and the start of another.
 */
public fun Schedule.adaptive(clock: Clock = Clock.System): Schedule {
    return transformSchedule { scheduleSequence ->
        sequence {
            // return the first item as-is
            val iterator = scheduleSequence.iterator()
            var current = iterator.next()
            yield(current)

            // for each subsequent item, calculate the time it took to `yield` the previous item, and delay by that
            // amount. Don't filter or buffer any values from the original sequence, just adjust their timing. Either
            // the upstream sequence should filter or returns values with a valid future time, or else the downstream
            // executor is responsible for handling backpressure or dropping values to keep up.
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
