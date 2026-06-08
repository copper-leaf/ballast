package com.copperleaf.ballast.scheduler.schedule

import com.copperleaf.ballast.scheduler.Schedule
import kotlin.math.pow
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Instant

/**
 * An exponential delay schedule will return a perfect schedule that delays a specific amount of time between tasks.
 * Each subsequent task will be delayed by [period] * [exponential]^n, where n is the number of times the task has been
 * scheduled so far. The delay will not exceed [maxDelay].
 *
 * Note that this schedule does not carry any state about how many times it has been invoked, so the exponential delay
 * is only compounded when iterating through the sequence returned by [generateSchedule]. Subsequent calls to
 * `generateSchedule` will always start the delay back at [period].
 */
public class ExponentialDelaySchedule(
    private val period: Duration,
    private val exponential: Double,
    private val maxDelay: Duration = period * 5.0.pow(exponential),
) : Schedule {

    init {
        check(period >= 1.milliseconds) {
            "Minimum period of delay is 1ms"
        }
        check(exponential > 1.0) {
            "exponential factor must be greater than 1.0"
        }
    }

    override fun generateSchedule(start: Instant): Sequence<Instant> {
        return sequence {
            var nextInstant = start
            var currentDelay = period

            while (true) {
                nextInstant += currentDelay
                currentDelay = minOf(currentDelay * exponential, maxDelay)
                yield(nextInstant)
            }
        }
    }
}
