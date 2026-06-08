package com.copperleaf.ballast.scheduler.operators

import com.copperleaf.ballast.scheduler.Schedule
import kotlin.time.Instant

/**
 * Only process scheduled tasks which are within the bounds (inclusive) of the [validRange]. Instants emitted before the
 * start of the range will be ignored, and the first Instant emitted after the end of the range will terminate the
 * sequence, making it finite.
 */
public fun Schedule.between(validRange: ClosedRange<Instant>): Schedule {
    check(!validRange.isEmpty()) {
        "the valid range of dates cannot be empty"
    }

    return transformSchedule { scheduleSequence ->
        sequence {
            val iterator = scheduleSequence.iterator()

            while (iterator.hasNext()) {
                val next = iterator.next()

                when {
                    next < validRange.start -> {
                        // we haven't entered the start of the range, don't quit yet
                        continue
                    }

                    next in validRange -> {
                        // we are withing the valid range, yield the values downstream
                        yield(next)
                    }

                    next > validRange.endInclusive -> {
                        // we are past the end of the range, quit the loop
                        break
                    }

                    else -> {
                        // not possible
                        break
                    }
                }
            }
        }
    }
}

public fun Schedule.startingAt(startInclusive: Instant): Schedule {
    return transformSchedule { scheduleSequence ->
        scheduleSequence.takeWhile { it >= startInclusive }
    }
}

public fun Schedule.until(endInclusive: Instant): Schedule {
    return transformSchedule { scheduleSequence ->
        scheduleSequence.takeWhile { it <= endInclusive }
    }
}
