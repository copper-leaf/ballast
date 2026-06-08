package com.copperleaf.ballast.scheduler.operators

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
import kotlin.time.Duration.Companion.hours

class TransformOperatorsTest {
    val timeZone = TimeZone.UTC
    val startDay = LocalDate(2023, Month.DECEMBER, 28)
    val startInstant = startDay.atTime(2, 37, 0).toInstant(timeZone)

    @Test
    fun scheduleTransformScheduleTest() = runTest {
        // transformSchedule allows arbitrary manipulation of the underlying Sequence, such as skipping every other item
        assertEquals(
            actual = EveryMinuteSchedule(12)
                .transformSchedule { seq -> seq.filterIndexed { index, _ -> index % 2 == 0 } }
                .take(4)
                .generateSchedule(startInstant)
                .firstTen(),
            expected = listOf(
                startDay.atTime(2, 37, 12),
                startDay.atTime(2, 39, 12),
                startDay.atTime(2, 41, 12),
                startDay.atTime(2, 43, 12),
            ),
        )
    }

    @Test
    fun scheduleTransformScheduleStartTest() = runTest {
        // transformScheduleStart shifts the start instant before generating the schedule
        assertEquals(
            actual = EveryMinuteSchedule(12)
                .transformScheduleStart { it + 1.hours }
                .take(4)
                .generateSchedule(startInstant)
                .firstTen(),
            expected = listOf(
                startDay.atTime(3, 37, 12),
                startDay.atTime(3, 38, 12),
                startDay.atTime(3, 39, 12),
                startDay.atTime(3, 40, 12),
            ),
        )
    }
}
