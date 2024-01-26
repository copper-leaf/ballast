package com.copperleaf.ballast.scheduler.schedule

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Duration.Companion.seconds

public class EverySecondSchedule private constructor(
    private val timeZone: TimeZone = TimeZone.UTC,
) : Schedule {

    override fun generateSchedule(start: Instant): Sequence<Instant> {
        return sequence {
            var nextInstant = start
            while (true) {
                nextInstant = nextInstant.getNextAvailableSecond()
                yield(nextInstant)
            }
        }
    }

    private fun Instant.getNextAvailableSecond(): Instant {
        return this
            .toLocalDateTime(timeZone)
            .nanosecond0()
            .toInstant(timeZone)
            .plus(1.seconds)
    }

    private fun LocalDateTime.nanosecond0(): LocalDateTime {
        return LocalDateTime(
            year = this.year,
            month = this.month,
            dayOfMonth = this.dayOfMonth,
            hour = this.hour,
            minute = this.minute,
            second = this.second,
            nanosecond = 0,
        )
    }

    public companion object {
        public operator fun invoke(
            timeZone: TimeZone = TimeZone.UTC,
        ): EverySecondSchedule {
            return EverySecondSchedule(timeZone)
        }
    }
}
