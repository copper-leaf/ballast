package com.copperleaf.ballast.scheduler.schedule

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Duration.Companion.days

public class EveryDaySchedule private constructor(
    private val timesOfDay: List<LocalTime>,
    private val timeZone: TimeZone = TimeZone.UTC,
) : Schedule {

    override fun generateSchedule(start: Instant): Sequence<Instant> {
        return sequence {
            var nextInstant = start
            while (true) {
                nextInstant = nextInstant.getNextAvailableTime()
                yield(nextInstant)
            }
        }
    }

    private fun Instant.getNextAvailableTime(): Instant {
        val currentInstantAsDateTime = this.toLocalDateTime(timeZone)

        val nextAvailableTime = timesOfDay
            .firstOrNull { it > currentInstantAsDateTime.time }

        return if (nextAvailableTime != null) {
            currentInstantAsDateTime
                .atTime(nextAvailableTime)
                .toInstant(timeZone)
        } else {
            this
                .plus(1.days)
                .toLocalDateTime(timeZone)
                .atTime(timesOfDay.first())
                .toInstant(timeZone)
        }
    }

    private fun LocalDateTime.atTime(time: LocalTime): LocalDateTime {
        return LocalDateTime(
            year = this.year,
            month = this.month,
            dayOfMonth = this.dayOfMonth,
            hour = time.hour,
            minute = time.minute,
            second = 0,
            nanosecond = 0,
        )
    }

    public companion object {
        public operator fun invoke(
            vararg timesOfDay: LocalTime = arrayOf(LocalTime.fromSecondOfDay(0)),
            timeZone: TimeZone = TimeZone.UTC,
        ): EveryDaySchedule {
            check(timesOfDay.isNotEmpty()) {
                "timesOfDay cannot be empty"
            }

            return EveryDaySchedule(timesOfDay.sorted(), timeZone)
        }
    }
}
