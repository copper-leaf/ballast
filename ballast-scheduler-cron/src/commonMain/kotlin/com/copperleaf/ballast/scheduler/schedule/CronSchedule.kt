package com.copperleaf.ballast.scheduler.schedule

import com.copperleaf.ballast.scheduler.Schedule
import kotlin.time.Instant

public data class CronSchedule(
    val expression: CronExpression,
) : Schedule {

    override fun generateSchedule(start: Instant): Sequence<Instant> {
        return generateSequence(
            expression.nextMatchingInstant(start)
        ) { prev ->
            expression.nextMatchingInstant(prev)
        }
    }
}
