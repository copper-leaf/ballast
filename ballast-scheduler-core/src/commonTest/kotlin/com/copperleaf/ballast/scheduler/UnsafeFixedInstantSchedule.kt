package com.copperleaf.ballast.scheduler

import kotlin.time.Instant

public class UnsafeFixedInstantSchedule(
    private val instants: List<Instant>,
) : Schedule {

    public constructor(
        vararg instants: Instant,
    ) : this(instants.toList())

    override fun generateSchedule(start: Instant): Sequence<Instant> {
        return sequence {
            val remainingInstants = instants.toMutableList()

            while (true) {
                val nextInstant = remainingInstants.removeFirstOrNull() ?: return@sequence
                yield(nextInstant)
            }
        }
    }
}
