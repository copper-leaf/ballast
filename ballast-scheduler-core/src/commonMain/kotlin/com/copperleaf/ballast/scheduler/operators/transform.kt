package com.copperleaf.ballast.scheduler.operators

import com.copperleaf.ballast.scheduler.Schedule
import kotlin.time.Instant

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
