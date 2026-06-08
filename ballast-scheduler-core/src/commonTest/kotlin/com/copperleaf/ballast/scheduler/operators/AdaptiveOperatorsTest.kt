package com.copperleaf.ballast.scheduler.operators

import com.copperleaf.ballast.scheduler.ExactTimeClock
import com.copperleaf.ballast.scheduler.firstTen
import com.copperleaf.ballast.scheduler.schedule.EveryMinuteSchedule
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlinx.datetime.toInstant
import kotlin.test.Test
import kotlin.test.assertEquals

class AdaptiveOperatorsTest {
    val timeZone = TimeZone.UTC
    val startDay = LocalDate(2023, Month.DECEMBER, 28)
    val startInstant = startDay.atTime(2, 37, 0).toInstant(timeZone)

    @Test
    fun scheduleAdaptiveTest_withDelay() = runTest {
        // When processing each task takes some time, adaptive shifts subsequent scheduled instants forward
        // by the amount of time elapsed since the task was supposed to start.
        val clock = ExactTimeClock(
            startDay.atTime(2, 38, 30).toInstant(timeZone), // clock.now() when computing 2nd item
            startDay.atTime(2, 39, 45).toInstant(timeZone), // clock.now() when computing 3rd item
        )

        assertEquals(
            actual = EveryMinuteSchedule(0)
                .adaptive(clock)
                .take(3)
                .generateSchedule(startInstant)
                .firstTen(),
            expected = listOf(
                startDay.atTime(2, 38, 0), // first item always returned as-is
                startDay.atTime(2, 39, 30), // 02:38:30 (now) + 60s (intended gap) = 02:39:30
                startDay.atTime(2, 40, 45), // 02:39:45 (now) + 60s (intended gap) = 02:40:45
            ),
        )
    }

    @Test
    fun scheduleAdaptiveTest_noDelay() = runTest {
        // When clock.now() returns the exact time of the current schedule item, the adaptive output
        // equals the original schedule (no adjustment needed).
        val clock = ExactTimeClock(
            startDay.atTime(2, 38, 0).toInstant(timeZone), // clock at exact schedule time
            startDay.atTime(2, 39, 0).toInstant(timeZone),
        )

        assertEquals(
            actual = EveryMinuteSchedule(0)
                .adaptive(clock)
                .take(3)
                .generateSchedule(startInstant)
                .firstTen(),
            expected = listOf(
                startDay.atTime(2, 38, 0),
                startDay.atTime(2, 39, 0),
                startDay.atTime(2, 40, 0),
            ),
        )
    }
}
