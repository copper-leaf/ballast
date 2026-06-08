package com.copperleaf.ballast.scheduler.schedule

import com.copperleaf.ballast.scheduler.Schedule
import kotlin.time.Instant

/**
 * A schedule which sends a specific sequence of [instants], rather than computing them. At each emission, the nearest
 * future Instant to the provided [clock] will be sent. When no such Instant exists, the schedule will complete.
 */
public class FixedInstantSchedule(
    instants: List<Instant>,
) : Schedule {

    private val instants: List<Instant>

    init {
        check(instants.isNotEmpty()) { "instants cannot be empty" }
        this.instants = instants.sorted()
    }

    public constructor(
        vararg instants: Instant,
    ) : this(instants.toList())

    override fun generateSchedule(start: Instant): Sequence<Instant> {
        return sequence {
            val remainingInstants = instants
                .dropWhile { it <= start }
                .toMutableList()

            while (true) {
                val nextInstant = remainingInstants.removeFirstOrNull() ?: return@sequence
                yield(nextInstant)
            }
        }
    }
}
