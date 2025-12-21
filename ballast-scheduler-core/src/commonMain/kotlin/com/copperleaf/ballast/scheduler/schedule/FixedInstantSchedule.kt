package com.copperleaf.ballast.scheduler.schedule

import kotlin.time.Clock
import kotlin.time.Instant

/**
 * A schedule which sends a specific sequence of [instants], rather than computing them. At each emission, the nearest
 * future Instant to the provided [clock] will be sent. When no such Instant exists, the schedule will complete.
 */
public class FixedInstantSchedule(
    instants: List<Instant>,
    private val clock: Clock,
) : Schedule {

    private val instants: List<Instant>

    init {
        check(instants.isNotEmpty()) { "instants cannot be empty" }
        this.instants = instants.sorted()
    }

    public constructor(
        vararg instants: Instant,
        clock: Clock
    ) : this(instants.toList(), clock)

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
}
