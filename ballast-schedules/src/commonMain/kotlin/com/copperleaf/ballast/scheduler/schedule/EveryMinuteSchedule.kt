package com.copperleaf.ballast.scheduler.schedule

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Duration.Companion.minutes

public class EveryMinuteSchedule private constructor(
    private val secondsOfMinute: List<Int>,
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
            dayOfMonth = this.dayOfMonth,
            hour = this.hour,
            minute = this.minute,
            second = second,
            nanosecond = 0,
        )
    }

    public companion object {
        public operator fun invoke(
            vararg secondsOfMinute: Int = IntArray(0),
            timeZone: TimeZone = TimeZone.UTC,
        ): EveryMinuteSchedule {
            check(secondsOfMinute.isNotEmpty()) {
                "secondsOfMinute cannot be empty"
            }
            check(secondsOfMinute.all { it in 0..59 }) {
                "all secondsOfMinute must be in range [0, 59]"
            }

            return EveryMinuteSchedule(secondsOfMinute.sorted(), timeZone)
        }
    }
}
