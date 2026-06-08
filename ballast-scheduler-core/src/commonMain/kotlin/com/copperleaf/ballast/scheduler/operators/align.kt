package com.copperleaf.ballast.scheduler.operators

import com.copperleaf.ballast.scheduler.Schedule
import com.copperleaf.ballast.scheduler.utils.alignToNextDay
import com.copperleaf.ballast.scheduler.utils.alignToNextHour
import com.copperleaf.ballast.scheduler.utils.alignToNextMinute
import com.copperleaf.ballast.scheduler.utils.alignToNextSecond
import kotlinx.datetime.TimeZone
import kotlin.time.DurationUnit

public fun Schedule.alignTo(unit: DurationUnit, timeZone: TimeZone = TimeZone.UTC): Schedule {
    return transformSchedule { scheduleSequence ->
        sequence {
            // return the first item as-is
            val iterator = scheduleSequence.iterator()

            // for each item, align it to the specified time unit boundary. Always ensure the resulting time is
            // greater than or equal to the original time.
            while (iterator.hasNext()) {
                val next = iterator.next()
                val alignedDateTime = when (unit) {
                    DurationUnit.SECONDS -> next.alignToNextSecond(timeZone)
                    DurationUnit.MINUTES -> next.alignToNextMinute(timeZone)
                    DurationUnit.HOURS -> next.alignToNextHour(timeZone)
                    DurationUnit.DAYS -> next.alignToNextDay(timeZone)
                    else -> {
                        error("Unsupported alignment unit: $unit")
                    }
                }

                yield(alignedDateTime)
            }
        }
    }
}
