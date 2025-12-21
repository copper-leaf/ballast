package com.copperleaf.ballast.scheduler.schedule

import com.copperleaf.ballast.scheduler.Schedule
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Duration.Companion.hours
import kotlin.time.Instant

public class EveryHourSchedule(
    minutesOfHour: List<Int> = listOf(0),
    private val timeZone: TimeZone = TimeZone.UTC,
) : Schedule {

    private val minutesOfHour: List<Int>

    init {
        check(minutesOfHour.isNotEmpty()) { "minutesOfHour cannot be empty" }
        check(minutesOfHour.all { it in 0..59 }) {
            "all secondsOfMinute must be in range [0, 59]"
        }

        this.minutesOfHour = minutesOfHour.sorted()
    }

    public constructor(
        vararg minutesOfHour: Int,
        timeZone: TimeZone = TimeZone.UTC,
    ) : this(minutesOfHour.toList(), timeZone)

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
            day = this.day,
            hour = this.hour,
            minute = minute,
            second = 0,
            nanosecond = 0,
        )
    }
}
