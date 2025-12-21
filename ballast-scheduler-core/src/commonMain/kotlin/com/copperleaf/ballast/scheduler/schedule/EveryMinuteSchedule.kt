package com.copperleaf.ballast.scheduler.schedule

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant

public class EveryMinuteSchedule(
    secondsOfMinute: List<Int> = listOf(0),
    private val timeZone: TimeZone = TimeZone.UTC,
) : Schedule {

    private val secondsOfMinute: List<Int>

    init {
        check(secondsOfMinute.isNotEmpty()) { "secondsOfMinute cannot be empty" }
        check(secondsOfMinute.all { it in 0..59 }) {
            "all secondsOfMinute must be in range [0, 59]"
        }

        this.secondsOfMinute = secondsOfMinute.sorted()
    }

    public constructor(
        vararg secondsOfMinute: Int,
        timeZone: TimeZone = TimeZone.UTC,
    ) : this(secondsOfMinute.toList(), timeZone)

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
        val currentInstantAsDateTime = this.toLocalDateTime(timeZone)

        val nextAvailableSecond = secondsOfMinute
            .firstOrNull { it > currentInstantAsDateTime.second }

        return if (nextAvailableSecond != null) {
            currentInstantAsDateTime
                .atSecond(nextAvailableSecond)
                .toInstant(timeZone)
        } else {
            this
                .plus(1.minutes)
                .toLocalDateTime(timeZone)
                .atSecond(secondsOfMinute.first())
                .toInstant(timeZone)
        }
    }

    private fun LocalDateTime.atSecond(second: Int): LocalDateTime {
        return LocalDateTime(
            year = this.year,
            month = this.month,
            day = this.day,
            hour = this.hour,
            minute = this.minute,
            second = second,
            nanosecond = 0,
        )
    }
}
