package com.copperleaf.ballast.scheduler.utils

import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlinx.datetime.toInstant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

class DateTimeUtilsTest {

    val timeZone = TimeZone.UTC
    val startDay = LocalDate(2023, Month.DECEMBER, 28)
    val startInstant = startDay.atTime(4, 5, 6, 7).toInstant(timeZone)

    @Test
    fun alignToSecondTest() = runTest {
        assertEquals(
            actual = startInstant.alignToNextSecond(timeZone),
            expected = LocalDateTime(2023, Month.DECEMBER, 28, 4, 5, 7, 0).toInstant(timeZone)
        )
    }

    @Test
    fun alignToMinuteTest() = runTest {
        assertEquals(
            actual = startInstant.alignToNextMinute(timeZone),
            expected = LocalDateTime(2023, Month.DECEMBER, 28, 4, 6, 0, 0).toInstant(timeZone)
        )
    }

    @Test
    fun alignToHourTest() = runTest {
        assertEquals(
            actual = startInstant.alignToNextHour(timeZone),
            expected = LocalDateTime(2023, Month.DECEMBER, 28, 5, 0, 0, 0).toInstant(timeZone)
        )
    }

    @Test
    fun alignToDayTest() = runTest {
        assertEquals(
            actual = startInstant.alignToNextDay(timeZone),
            expected = LocalDateTime(2023, Month.DECEMBER, 29, 0, 0, 0, 0).toInstant(timeZone)
        )
    }

    @Test
    fun isSameOrBeforeMinuteTest() = runTest {
        assertTrue {
            startInstant.isSameOrBeforeMinute(startInstant, timeZone)
        }
        assertTrue {
            startInstant.minus(1.seconds).isSameOrBeforeMinute(startInstant, timeZone)
        }
        assertTrue {
            startInstant.plus(1.seconds).isSameOrBeforeMinute(startInstant, timeZone)
        }
        assertTrue {
            startInstant.minus(1.minutes).isSameOrBeforeMinute(startInstant, timeZone)
        }
        assertTrue {
            startInstant.minus(1.hours).isSameOrBeforeMinute(startInstant, timeZone)
        }
        assertTrue {
            startInstant.minus(1.days).isSameOrBeforeMinute(startInstant, timeZone)
        }

        assertFalse {
            startInstant.plus(1.minutes).isSameOrBeforeMinute(startInstant, timeZone)
        }
        assertFalse {
            startInstant.plus(1.hours).isSameOrBeforeMinute(startInstant, timeZone)
        }
        assertFalse {
            startInstant.plus(1.days).isSameOrBeforeMinute(startInstant, timeZone)
        }
    }
}
