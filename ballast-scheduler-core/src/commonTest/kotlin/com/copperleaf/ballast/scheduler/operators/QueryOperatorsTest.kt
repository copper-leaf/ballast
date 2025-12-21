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
import kotlin.time.Clock
import kotlin.time.Instant

class QueryOperatorsTest {
    val timeZone = TimeZone.UTC
    val startDay = LocalDate(2023, Month.DECEMBER, 28)
    val startInstant = startDay.atTime(2, 37, 0).toInstant(timeZone)
    val currentInstant = startDay.atTime(2, 44, 0).toInstant(timeZone)

    @Test
    fun scheduleTakeTest() = runTest {
        assertEquals(
            actual = EveryMinuteSchedule(12)
                .take(4)
                .generateSchedule(startInstant)
                .firstTen(),
            expected = listOf(
                startDay.atTime(2, 37, 12),
                startDay.atTime(2, 38, 12),
                startDay.atTime(2, 39, 12),
                startDay.atTime(2, 40, 12),
            ),
        )
    }

    @Test
    fun scheduleGetNextTest() = runTest {
        val clock = object : Clock {
            override fun now(): Instant {
                return startInstant
            }
        }

        assertEquals(
            actual = EveryMinuteSchedule(5, timeZone = timeZone).getNext(clock),
            expected = startDay.atTime(2, 37, 5).toInstant(timeZone),
        )

        assertEquals(
            actual = EveryMinuteSchedule(5, timeZone = timeZone).getNext(startInstant),
            expected = startDay.atTime(2, 37, 5).toInstant(timeZone),
        )
    }

    @Test
    fun scheduleGetHistoryUnboundedTest() = runTest {
        assertEquals(
            actual = EveryMinuteSchedule(12)
                .getHistory(
                    startInstant = startInstant,
                    currentInstant = currentInstant,
                )
                .toList(),
            expected = listOf(
                startDay.atTime(2, 37, 12).toInstant(timeZone),
                startDay.atTime(2, 38, 12).toInstant(timeZone),
                startDay.atTime(2, 39, 12).toInstant(timeZone),
                startDay.atTime(2, 40, 12).toInstant(timeZone),
                startDay.atTime(2, 41, 12).toInstant(timeZone),
                startDay.atTime(2, 42, 12).toInstant(timeZone),
                startDay.atTime(2, 43, 12).toInstant(timeZone),
            ),
        )
    }

    @Test
    fun scheduleGetHistoryBoundedTest() = runTest {
        assertEquals(
            actual = EveryMinuteSchedule(12)
                .take(3)
                .getHistory(
                    startInstant = startInstant,
                    currentInstant = currentInstant,
                )
                .toList(),
            expected = listOf(
                startDay.atTime(2, 37, 12).toInstant(timeZone),
                startDay.atTime(2, 38, 12).toInstant(timeZone),
                startDay.atTime(2, 39, 12).toInstant(timeZone),
            ),
        )
    }

    @Test
    fun scheduleDropHistoryUnboundedTest() = runTest {
        val scheduleSequence = EveryMinuteSchedule(12)
            .dropHistory(
                startInstant = startInstant,
                currentInstant = currentInstant,
            )
            .take(4)
            .toList()

        assertEquals(
            listOf(
                startDay.atTime(2, 44, 12).toInstant(timeZone),
                startDay.atTime(2, 45, 12).toInstant(timeZone),
                startDay.atTime(2, 46, 12).toInstant(timeZone),
                startDay.atTime(2, 47, 12).toInstant(timeZone),
            ), scheduleSequence
        )
    }

    @Test
    fun scheduleDropHistoryBoundedTest() = runTest {
        val scheduleSequence = EveryMinuteSchedule(12)
            .take(3)
            .dropHistory(
                startInstant = startInstant,
                currentInstant = currentInstant,
            )
            .take(4)
            .toList()

        assertEquals(0, scheduleSequence.size)
    }
}
