package com.copperleaf.ballast.scheduler.schedule

import kotlinx.datetime.Instant
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

/**
 * A fixed delay schedule will return a perfect schedule that delays a specific amount of time between tasks. By
 * default, the delay does not consider how long it takes to process each task, and they may be dropped if the
 * processing time is longer than the schedule period.
 *
 * Use the [adaptive] schedule operator to make the schedule adapt to the processing time of an item, so that the
 * specified amount of time is delayed between the end of processing one task and the next time it begins.
 */
public class FixedDelaySchedule private constructor(
    private val period: Duration
) : Schedule {
    override fun generateSchedule(start: Instant): Sequence<Instant> {
        return sequence {
            var nextInstant = start
            while (true) {
                nextInstant += period
                yield(nextInstant)
            }
        }
    }

    public companion object {
        public operator fun invoke(delay: Duration): FixedDelaySchedule {
            check(delay >= 1.milliseconds) {
                "Minimum delay is 1ms"
            }

            return FixedDelaySchedule(delay)
        }
    }
}
