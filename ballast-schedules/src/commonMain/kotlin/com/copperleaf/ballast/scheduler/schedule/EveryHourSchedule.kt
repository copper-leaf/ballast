package com.copperleaf.ballast.scheduler.schedule

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Duration.Companion.hours

public class EveryHourSchedule private constructor(
    private val minutesOfHour: List<Int>,
    private val timeZone: TimeZone = TimeZone.UTC,
) : Schedule {

    override fun generateSchedule(start: Instant): Sequence<Instant> {
        return sequence {
            var nextInstant = start
            while (true) {
                nextInstant = nextInstant.getNextAvailableMinute()
                yield(nextInstant)
            }
        }
    }

    private fun Instant.getNextAvailableMinute(): Instant {
        val currentInstantAsDateTime = this.toLocalDateTime(timeZone)

        val nextAvailableMinute = minutesOfHour
            .firstOrNull { it > currentInstantAsDateTime.minute }

        return if (nextAvailableMinute != null) {
            currentInstantAsDateTime
                .atMinute(nextAvailableMinute)
                .toInstant(timeZone)
        } else {
            this
                .plus(1.hours)
                .toLocalDateTime(timeZone)
                .atMinute(minutesOfHour.first())
                .toInstant(timeZone)
        }
    }

    private fun LocalDateTime.atMinute(minute: Int): LocalDateTime {
        return LocalDateTime(
            year = this.year,
            month = this.month,
            dayOfMonth = this.dayOfMonth,
            hour = this.hour,
            minute = minute,
            second = 0,
            nanosecond = 0,
        )
    }

    public companion object {
        public operator fun invoke(
            vararg minutesOfHour: Int = IntArray(0),
            timeZone: TimeZone = TimeZone.UTC,
        ): EveryHourSchedule {
            check(minutesOfHour.isNotEmpty()) {
                "minutesOfHour cannot be empty"
            }
            check(minutesOfHour.all { it in 0..59 }) {
                "all minutesOfHour must be in range [0, 59]"
            }

            return EveryHourSchedule(minutesOfHour.sorted(), timeZone)
        }
    }
}
