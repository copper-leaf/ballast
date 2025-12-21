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

class FixedInstantScheduleTest {
    val timeZone = TimeZone.UTC
    val startDay = LocalDate(2023, Month.DECEMBER, 28)
    val startInstant = startDay.atTime(2, 37, 52).toInstant(timeZone)

    @Test
    fun oneFixedInstant() = runTest {
        assertEquals(
            actual = FixedInstantSchedule(
                startDay.atTime(2, 45, 0).toInstant(timeZone),
            )
                .generateSchedule(startInstant)
                .firstTen(),
            expected = listOf(
                startDay.atTime(2, 45, 0),
            ),
        )
    }

    @Test
    fun multipleFixedInstants() = runTest {
        assertEquals(
            actual = FixedInstantSchedule(
                startDay.atTime(2, 45, 0).toInstant(timeZone),
                startDay.atTime(3, 45, 0).toInstant(timeZone),
                startDay.atTime(3, 56, 44).toInstant(timeZone),
            )
                .generateSchedule(startInstant)
                .firstTen(),
            expected = listOf(
                startDay.atTime(2, 45, 0),
                startDay.atTime(3, 45, 0),
                startDay.atTime(3, 56, 44),
            ),
        )
    }
}
