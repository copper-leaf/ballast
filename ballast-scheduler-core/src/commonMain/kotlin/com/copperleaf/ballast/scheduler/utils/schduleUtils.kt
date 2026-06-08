package com.copperleaf.ballast.scheduler.utils

import com.copperleaf.ballast.scheduler.Schedule
import kotlin.time.Instant

/**
 * Generates a schedule starting from [start], ensuring that the first generated time is always strictly after [start],
 * and that vales are always monotonically increasing and never repeated..
 */
public fun Schedule.generateSafeSchedule(start: Instant): Sequence<Instant> {
    val scheduleDelegate = this
    return sequence {
        var latestEmission: Instant? = null

        scheduleDelegate
            .generateSchedule(start)
            .forEach { next ->
                if (latestEmission == null) {
                    // first emission, ensure it's strictly after start
                    if (next > start) {
                        latestEmission = next
                        yield(next)
                    } else {
                        error("Schedule $scheduleDelegate generated a first emission ($next) that is not strictly after the schedule start time ($start)")
                    }
                } else {
                    // subsequent emissions, ensure they're strictly after the last one
                    if (next > latestEmission) {
                        latestEmission = next
                        yield(next)
                    } else {
                        error("Schedule $scheduleDelegate generated a non-monotonic emission ($next) after previous emission ($latestEmission)")
                    }
                }
            }
    }
}
