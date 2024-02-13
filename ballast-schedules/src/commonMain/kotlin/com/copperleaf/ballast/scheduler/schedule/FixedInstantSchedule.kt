package com.copperleaf.ballast.scheduler.schedule

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

/**
 * A schedule which sends a specific sequence of [instants], rather than computing them. At each emission, the nearest
 * future Instant to the provided [clock] will be sent. When no such Instant exists, the schedule will complete.
 */
public class FixedInstantSchedule private constructor(
    private val instants: List<Instant>,
    private val clock: Clock,
) : Schedule {
    override fun generateSchedule(start: Instant): Sequence<Instant> {
        return sequence {
            while (true) {
                val now = clock.now()
                val nextInstant = getNextInstant(now) ?: return@sequence
                yield(nextInstant)
            }
        }
    }

    private fun getNextInstant(now: Instant): Instant? {
        return instants.firstOrNull { it > now }
    }

    public companion object {
        public operator fun invoke(instants: List<Instant>, clock: Clock = Clock.System): FixedInstantSchedule {
            return FixedInstantSchedule(
                instants = instants.distinct().sorted(),
                clock = clock,
            )
        }

        public operator fun invoke(vararg instants: Instant, clock: Clock = Clock.System): FixedInstantSchedule {
            return FixedInstantSchedule(
                instants = instants.distinct().sorted(),
                clock = clock
            )
        }
    }
}
