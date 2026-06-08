package com.copperleaf.ballast.scheduler

import kotlin.time.Clock
import kotlin.time.Instant

class ExactTimeClock(
    vararg instants: Instant,
) : Clock {
    private val instantSequence = instants.sorted().toMutableList()

    override fun now(): Instant {
        return runCatching {
            val next = instantSequence.first()
            instantSequence.removeAt(0)
            next
        }.getOrElse { Instant.DISTANT_FUTURE }
    }
}
