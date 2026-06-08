package com.copperleaf.ballast.scheduler.schedule

import com.copperleaf.ballast.scheduler.firstTen
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlinx.datetime.toInstant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.minutes

class FixedDelayScheduleTest {
    val timeZone = TimeZone.UTC
    val startDay = LocalDate(2023, Month.DECEMBER, 28)
    val startInstant = startDay.atTime(1, 1).toInstant(timeZone)

    @Test
    fun fixedDelayScheduleTest() = runTest {
        assertEquals(
            actual = FixedDelaySchedule(10.minutes)
                .generateSchedule(startInstant)
                .firstTen(),
            expected = listOf(
                startDay.atTime(1, 11),
                startDay.atTime(1, 21),
                startDay.atTime(1, 31),
                startDay.atTime(1, 41),
                startDay.atTime(1, 51),
                startDay.atTime(2, 1),
                startDay.atTime(2, 11),
                startDay.atTime(2, 21),
                startDay.atTime(2, 31),
                startDay.atTime(2, 41),
            ),
        )
    }
}
