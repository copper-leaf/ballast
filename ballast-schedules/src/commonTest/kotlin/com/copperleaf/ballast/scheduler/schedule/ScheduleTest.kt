package com.copperleaf.ballast.scheduler.schedule

import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.atTime
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

class ScheduleTest {
    val timeZone = TimeZone.UTC
    val startDay = LocalDate(2023, Month.DECEMBER, 28)
    val startInstant = startDay.atStartOfDayIn(timeZone)
    fun Sequence<Instant>.firstTen(): List<LocalDateTime> {
        return this
            .map { it.toLocalDateTime(timeZone) }
            .take(10)
            .toList()
    }

    @Test
    fun fixedDelayScheduleTest() = runTest {
        val generatedSchedule =
            FixedDelaySchedule(10.minutes)
                .generateSchedule(
                    start = startDay.atTime(1, 1).toInstant(timeZone)
                )
                .firstTen()

        assertEquals<Any?>(
            listOf(
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
            ), generatedSchedule
        )
    }

// Once per schedule resolution
// ---------------------------------------------------------------------------------------------------------------------

    @Test
    fun onceEveryDayTest() = runTest {
        val generatedSchedule =
            EveryDaySchedule(LocalTime(2, 37))
                .generateSchedule(
                    start = startDay.atTime(1, 0).toInstant(timeZone)
                )
                .firstTen()

        assertEquals<Any?>(
            listOf(
                LocalDate(2023, Month.DECEMBER, 28).atTime(2, 37),
                LocalDate(2023, Month.DECEMBER, 29).atTime(2, 37),
                LocalDate(2023, Month.DECEMBER, 30).atTime(2, 37),
                LocalDate(2023, Month.DECEMBER, 31).atTime(2, 37),
                LocalDate(2024, Month.JANUARY, 1).atTime(2, 37),
                LocalDate(2024, Month.JANUARY, 2).atTime(2, 37),
                LocalDate(2024, Month.JANUARY, 3).atTime(2, 37),
                LocalDate(2024, Month.JANUARY, 4).atTime(2, 37),
                LocalDate(2024, Month.JANUARY, 5).atTime(2, 37),
                LocalDate(2024, Month.JANUARY, 6).atTime(2, 37),
            ), generatedSchedule
        )
    }

    @Test
    fun onceEveryHourTest() = runTest {
        val generatedSchedule =
            EveryHourSchedule(1)
                .generateSchedule(
                    start = startDay.atTime(2, 37).toInstant(timeZone)
                )
                .firstTen()

        assertEquals<Any?>(
            listOf(
                startDay.atTime(3, 1),
                startDay.atTime(4, 1),
                startDay.atTime(5, 1),
                startDay.atTime(6, 1),
                startDay.atTime(7, 1),
                startDay.atTime(8, 1),
                startDay.atTime(9, 1),
                startDay.atTime(10, 1),
                startDay.atTime(11, 1),
                startDay.atTime(12, 1),
            ), generatedSchedule
        )
    }

    @Test
    fun onceEveryMinuteTest() = runTest {
        val scheduleSequence = EveryMinuteSchedule(12)
            .generateSchedule(
                start = startDay.atTime(2, 37, 0).toInstant(timeZone)
            )
            .firstTen()

        assertEquals<Any?>(
            listOf(
                startDay.atTime(2, 37, 12),
                startDay.atTime(2, 38, 12),
                startDay.atTime(2, 39, 12),
                startDay.atTime(2, 40, 12),
                startDay.atTime(2, 41, 12),
                startDay.atTime(2, 42, 12),
                startDay.atTime(2, 43, 12),
                startDay.atTime(2, 44, 12),
                startDay.atTime(2, 45, 12),
                startDay.atTime(2, 46, 12),
            ), scheduleSequence
        )
    }

    @Test
    fun everySecondTest() = runTest {
        val scheduleSequence = EverySecondSchedule()
            .generateSchedule(
                start = startDay.atTime(2, 37, 52).toInstant(timeZone)
            )
            .firstTen()

        assertEquals<Any?>(
            listOf(
                startDay.atTime(2, 37, 53),
                startDay.atTime(2, 37, 54),
                startDay.atTime(2, 37, 55),
                startDay.atTime(2, 37, 56),
                startDay.atTime(2, 37, 57),
                startDay.atTime(2, 37, 58),
                startDay.atTime(2, 37, 59),
                startDay.atTime(2, 38, 0),
                startDay.atTime(2, 38, 1),
                startDay.atTime(2, 38, 2),
            ), scheduleSequence
        )
    }

    @Test
    fun oneFixedInstant() = runTest {
        val clock = object : Clock {
            val instantSequence = mutableListOf(
                startInstant
            )

            override fun now(): Instant {
                return runCatching { instantSequence.removeFirst() }
                    .getOrElse { Instant.DISTANT_FUTURE }
            }
        }
        val scheduleSequence = FixedInstantSchedule(
            startDay.atTime(2, 45, 0).toInstant(timeZone),
            clock = clock,
        )
            .generateSchedule(
                start = startDay.atTime(2, 37, 52).toInstant(timeZone)
            )
            .firstTen()

        assertEquals<Any?>(
            listOf(
                startDay.atTime(2, 45, 0),
            ), scheduleSequence
        )
    }

// Multiple times per schedule resolution
// ---------------------------------------------------------------------------------------------------------------------

    @Test
    fun multipleTimesEveryDayTest() = runTest {
        val generatedSchedule =
            EveryDaySchedule(LocalTime(2, 37), LocalTime(7, 38), LocalTime(23, 58))
                .generateSchedule(
                    start = startDay.atTime(1, 0).toInstant(timeZone)
                )
                .firstTen()

        assertEquals<Any?>(
            listOf(
                LocalDate(2023, Month.DECEMBER, 28).atTime(2, 37),
                LocalDate(2023, Month.DECEMBER, 28).atTime(7, 38),
                LocalDate(2023, Month.DECEMBER, 28).atTime(23, 58),
                LocalDate(2023, Month.DECEMBER, 29).atTime(2, 37),
                LocalDate(2023, Month.DECEMBER, 29).atTime(7, 38),
                LocalDate(2023, Month.DECEMBER, 29).atTime(23, 58),
                LocalDate(2023, Month.DECEMBER, 30).atTime(2, 37),
                LocalDate(2023, Month.DECEMBER, 30).atTime(7, 38),
                LocalDate(2023, Month.DECEMBER, 30).atTime(23, 58),
                LocalDate(2023, Month.DECEMBER, 31).atTime(2, 37),
            ), generatedSchedule
        )
    }

    @Test
    fun multipleTimesEveryHourTest() = runTest {
        val generatedSchedule =
            EveryHourSchedule(0, 15, 30, 45)
                .generateSchedule(
                    start = startDay.atTime(2, 37).toInstant(timeZone)
                )
                .firstTen()

        assertEquals<Any?>(
            listOf(
                startDay.atTime(2, 45),
                startDay.atTime(3, 0),
                startDay.atTime(3, 15),
                startDay.atTime(3, 30),
                startDay.atTime(3, 45),
                startDay.atTime(4, 0),
                startDay.atTime(4, 15),
                startDay.atTime(4, 30),
                startDay.atTime(4, 45),
                startDay.atTime(5, 0),
            ), generatedSchedule
        )
    }

    @Test
    fun multipleTimesEveryMinuteTest() = runTest {
        val generatedSchedule = EveryMinuteSchedule(0, 15, 30, 45)
            .generateSchedule(
                start = startDay.atTime(2, 37, 0).toInstant(timeZone)
            )
            .firstTen()

        assertEquals<Any?>(
            listOf(
                startDay.atTime(2, 37, 15),
                startDay.atTime(2, 37, 30),
                startDay.atTime(2, 37, 45),
                startDay.atTime(2, 38, 0),
                startDay.atTime(2, 38, 15),
                startDay.atTime(2, 38, 30),
                startDay.atTime(2, 38, 45),
                startDay.atTime(2, 39, 0),
                startDay.atTime(2, 39, 15),
                startDay.atTime(2, 39, 30),
            ), generatedSchedule
        )
    }

    @Test
    fun multipleFixedInstants() = runTest {
        val clock = object : Clock {
            val instantSequence = mutableListOf(
                startDay.atTime(2, 44, 0).toInstant(timeZone),
                startDay.atTime(3, 44, 0).toInstant(timeZone),
                startDay.atTime(3, 55, 44).toInstant(timeZone),
            )

            override fun now(): Instant {
                return runCatching { instantSequence.removeFirst() }
                    .getOrElse { Instant.DISTANT_FUTURE }
            }
        }
        val scheduleSequence = FixedInstantSchedule(
            startDay.atTime(2, 45, 0).toInstant(timeZone),
            startDay.atTime(3, 45, 0).toInstant(timeZone),
            startDay.atTime(3, 56, 44).toInstant(timeZone),
            clock = clock,
        )
            .generateSchedule(
                start = startDay.atTime(2, 37, 52).toInstant(timeZone)
            )
            .firstTen()

        assertEquals<Any?>(
            listOf(
                startDay.atTime(2, 45, 0),
                startDay.atTime(3, 45, 0),
                startDay.atTime(3, 56, 44),
            ), scheduleSequence
        )
    }

// Schedule operators
// ---------------------------------------------------------------------------------------------------------------------

    @Test
    fun scheduleDelayedTest() = runTest {
        val scheduleSequence = EveryMinuteSchedule(12)
            .delayed(1.hours)
            .take(4)
            .generateSchedule(
                start = startDay.atTime(2, 37, 0).toInstant(timeZone)
            )
            .firstTen()

        assertEquals<Any?>(
            listOf(
                startDay.atTime(3, 37, 12),
                startDay.atTime(3, 38, 12),
                startDay.atTime(3, 39, 12),
                startDay.atTime(3, 40, 12),
            ), scheduleSequence
        )
    }

    @Test
    fun scheduleDelayedUntilTest_earlierThanActualStartTime() = runTest {
        val scheduleSequence = EveryMinuteSchedule(12)
            .delayedUntil(startDay.atTime(1, 0, 0).toInstant(timeZone))
            .generateSchedule(
                start = startDay.atTime(2, 37, 0).toInstant(timeZone)
            )
            .firstTen()

        assertEquals<Any?>(
            listOf(
                startDay.atTime(2, 37, 12),
                startDay.atTime(2, 38, 12),
                startDay.atTime(2, 39, 12),
                startDay.atTime(2, 40, 12),
                startDay.atTime(2, 41, 12),
                startDay.atTime(2, 42, 12),
                startDay.atTime(2, 43, 12),
                startDay.atTime(2, 44, 12),
                startDay.atTime(2, 45, 12),
                startDay.atTime(2, 46, 12),
            ), scheduleSequence
        )
    }

    @Test
    fun scheduleDelayedUntilTest_laterThanActualStartTime() = runTest {
        val scheduleSequence = EveryMinuteSchedule(12)
            .delayedUntil(startDay.atTime(4, 0, 0).toInstant(timeZone))
            .generateSchedule(
                start = startDay.atTime(2, 37, 0).toInstant(timeZone)
            )
            .firstTen()

        assertEquals<Any?>(
            listOf(
                startDay.atTime(4, 0, 12),
                startDay.atTime(4, 1, 12),
                startDay.atTime(4, 2, 12),
                startDay.atTime(4, 3, 12),
                startDay.atTime(4, 4, 12),
                startDay.atTime(4, 5, 12),
                startDay.atTime(4, 6, 12),
                startDay.atTime(4, 7, 12),
                startDay.atTime(4, 8, 12),
                startDay.atTime(4, 9, 12),
            ), scheduleSequence
        )
    }

    @Test
    fun scheduleBoundedTest_startsBeforeWindow() = runTest {
        val startMinute = startDay.atTime(2, 37, 0).toInstant(timeZone)
        val endMinute = startDay.atTime(2, 41, 0).toInstant(timeZone)

        val scheduleSequence = EveryMinuteSchedule(12)
            .bounded(startMinute..endMinute)
            .generateSchedule(
                start = startDay.atTime(2, 33, 0).toInstant(timeZone)
            )
            .firstTen()

        assertEquals<Any?>(
            listOf(
                startDay.atTime(2, 37, 12),
                startDay.atTime(2, 38, 12),
                startDay.atTime(2, 39, 12),
                startDay.atTime(2, 40, 12),
            ), scheduleSequence
        )
    }

    @Test
    fun scheduleBoundedTest_startsDuringWindow() = runTest {
        val startMinute = startDay.atTime(2, 37, 0).toInstant(timeZone)
        val endMinute = startDay.atTime(2, 41, 0).toInstant(timeZone)

        val scheduleSequence = EveryMinuteSchedule(12)
            .bounded(startMinute..endMinute)
            .generateSchedule(
                start = startDay.atTime(2, 39, 0).toInstant(timeZone)
            )
            .firstTen()

        assertEquals<Any?>(
            listOf(
                startDay.atTime(2, 39, 12),
                startDay.atTime(2, 40, 12),
            ), scheduleSequence
        )
    }

    @Test
    fun scheduleBoundedTest_startsAfterWindow() = runTest {
        val startMinute = startDay.atTime(2, 37, 0).toInstant(timeZone)
        val endMinute = startDay.atTime(2, 41, 0).toInstant(timeZone)

        val scheduleSequence = EveryMinuteSchedule(12)
            .bounded(startMinute..endMinute)
            .generateSchedule(
                start = startDay.atTime(2, 45, 0).toInstant(timeZone)
            )
            .firstTen()

        assertEquals(0, scheduleSequence.size)
    }

    @Test
    fun scheduleFilterByDayOfWeekTest() = runTest {
        val scheduleSequence = EveryDaySchedule(LocalTime(9, 0))
            .filterByDayOfWeek(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY, timeZone = timeZone)
            .generateSchedule(
                start = startDay.atTime(2, 45, 0).toInstant(timeZone)
            )
            .firstTen()

        assertEquals<Any?>(
            listOf(
                LocalDate(2023, Month.DECEMBER, 29).atTime(9, 0),
                LocalDate(2024, Month.JANUARY, 1).atTime(9, 0),
                LocalDate(2024, Month.JANUARY, 3).atTime(9, 0),
                LocalDate(2024, Month.JANUARY, 5).atTime(9, 0),
                LocalDate(2024, Month.JANUARY, 8).atTime(9, 0),
                LocalDate(2024, Month.JANUARY, 10).atTime(9, 0),
                LocalDate(2024, Month.JANUARY, 12).atTime(9, 0),
                LocalDate(2024, Month.JANUARY, 15).atTime(9, 0),
                LocalDate(2024, Month.JANUARY, 17).atTime(9, 0),
                LocalDate(2024, Month.JANUARY, 19).atTime(9, 0),
            ), scheduleSequence
        )
    }

    @Test
    fun scheduleWeekdaysTest() = runTest {
        val scheduleSequence = EveryDaySchedule(LocalTime(9, 0))
            .weekdays(timeZone = timeZone)
            .generateSchedule(
                start = startDay.atTime(2, 45, 0).toInstant(timeZone)
            )
            .firstTen()

        assertEquals<Any?>(
            listOf(
                LocalDate(2023, Month.DECEMBER, 28).atTime(9, 0),
                LocalDate(2023, Month.DECEMBER, 29).atTime(9, 0),
                LocalDate(2024, Month.JANUARY, 1).atTime(9, 0),
                LocalDate(2024, Month.JANUARY, 2).atTime(9, 0),
                LocalDate(2024, Month.JANUARY, 3).atTime(9, 0),
                LocalDate(2024, Month.JANUARY, 4).atTime(9, 0),
                LocalDate(2024, Month.JANUARY, 5).atTime(9, 0),
                LocalDate(2024, Month.JANUARY, 8).atTime(9, 0),
                LocalDate(2024, Month.JANUARY, 9).atTime(9, 0),
                LocalDate(2024, Month.JANUARY, 10).atTime(9, 0),
            ), scheduleSequence
        )
    }

    @Test
    fun scheduleWeekendsTest() = runTest {
        val scheduleSequence = EveryDaySchedule(LocalTime(9, 0))
            .weekends(timeZone = timeZone)
            .generateSchedule(
                start = startDay.atTime(2, 45, 0).toInstant(timeZone)
            )
            .firstTen()

        assertEquals<Any?>(
            listOf(
                LocalDate(2023, Month.DECEMBER, 30).atTime(9, 0),
                LocalDate(2023, Month.DECEMBER, 31).atTime(9, 0),
                LocalDate(2024, Month.JANUARY, 6).atTime(9, 0),
                LocalDate(2024, Month.JANUARY, 7).atTime(9, 0),
                LocalDate(2024, Month.JANUARY, 13).atTime(9, 0),
                LocalDate(2024, Month.JANUARY, 14).atTime(9, 0),
                LocalDate(2024, Month.JANUARY, 20).atTime(9, 0),
                LocalDate(2024, Month.JANUARY, 21).atTime(9, 0),
                LocalDate(2024, Month.JANUARY, 27).atTime(9, 0),
                LocalDate(2024, Month.JANUARY, 28).atTime(9, 0),
            ), scheduleSequence
        )
    }

    @Test
    fun scheduleUntilTest_startsBefore() = runTest {
        val endMinute = startDay.atTime(2, 41, 0).toInstant(timeZone)

        val scheduleSequence = EveryMinuteSchedule(12)
            .until(endMinute)
            .generateSchedule(
                start = startDay.atTime(2, 37, 0).toInstant(timeZone)
            )
            .firstTen()

        assertEquals<Any?>(
            listOf(
                startDay.atTime(2, 37, 12),
                startDay.atTime(2, 38, 12),
                startDay.atTime(2, 39, 12),
                startDay.atTime(2, 40, 12),
            ), scheduleSequence
        )
    }

    @Test
    fun scheduleUntilTest_startsAfter() = runTest {
        val endMinute = startDay.atTime(2, 41, 0).toInstant(timeZone)

        val scheduleSequence = EveryMinuteSchedule(12)
            .until(endMinute)
            .generateSchedule(
                start = startDay.atTime(2, 45, 0).toInstant(timeZone)
            )
            .firstTen()

        assertEquals(0, scheduleSequence.size)
    }

    @Test
    fun scheduleTakeTest() = runTest {
        val scheduleSequence = EveryMinuteSchedule(12)
            .take(4)
            .generateSchedule(
                start = startDay.atTime(2, 37, 0).toInstant(timeZone)
            )
            .firstTen()

        assertEquals<Any?>(
            listOf(
                startDay.atTime(2, 37, 12),
                startDay.atTime(2, 38, 12),
                startDay.atTime(2, 39, 12),
                startDay.atTime(2, 40, 12),
            ), scheduleSequence
        )
    }

    @Test
    fun scheduleGetNextTest() = runTest {
        val clock = object : Clock {
            override fun now(): Instant {
                return startInstant
            }
        }

        assertEquals<Any?>(
            startDay.atTime(0, 0, 5).toInstant(timeZone),
            EveryMinuteSchedule(5, timeZone = timeZone).getNext(clock)
        )

        assertEquals<Any?>(
            startDay.atTime(0, 0, 5).toInstant(timeZone),
            EveryMinuteSchedule(5, timeZone = timeZone).getNext(startInstant)
        )
    }

    @Test
    fun scheduleGetHistoryUnboundedTest() = runTest {
        val scheduleSequence = EveryMinuteSchedule(12)
            .getHistory(
                startInstant = startDay.atTime(2, 37, 0).toInstant(timeZone),
                currentInstant = startDay.atTime(2, 44, 0).toInstant(timeZone),
            )
            .toList()

        assertEquals<Any?>(
            listOf(
                startDay.atTime(2, 37, 12).toInstant(timeZone),
                startDay.atTime(2, 38, 12).toInstant(timeZone),
                startDay.atTime(2, 39, 12).toInstant(timeZone),
                startDay.atTime(2, 40, 12).toInstant(timeZone),
                startDay.atTime(2, 41, 12).toInstant(timeZone),
                startDay.atTime(2, 42, 12).toInstant(timeZone),
                startDay.atTime(2, 43, 12).toInstant(timeZone),
            ), scheduleSequence
        )
    }

    @Test
    fun scheduleGetHistoryBoundedTest() = runTest {
        val scheduleSequence = EveryMinuteSchedule(12)
            .take(3)
            .getHistory(
                startInstant = startDay.atTime(2, 37, 0).toInstant(timeZone),
                currentInstant = startDay.atTime(2, 44, 0).toInstant(timeZone),
            )
            .toList()

        assertEquals<Any?>(
            listOf(
                startDay.atTime(2, 37, 12).toInstant(timeZone),
                startDay.atTime(2, 38, 12).toInstant(timeZone),
                startDay.atTime(2, 39, 12).toInstant(timeZone),
            ), scheduleSequence
        )
    }

    @Test
    fun scheduleDropHistoryUnboundedTest() = runTest {
        val scheduleSequence = EveryMinuteSchedule(12)
            .dropHistory(
                startInstant = startDay.atTime(2, 37, 0).toInstant(timeZone),
                currentInstant = startDay.atTime(2, 44, 0).toInstant(timeZone),
            )
            .take(4)
            .toList()

        assertEquals<Any?>(
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
                startInstant = startDay.atTime(2, 37, 0).toInstant(timeZone),
                currentInstant = startDay.atTime(2, 44, 0).toInstant(timeZone),
            )
            .take(4)
            .toList()

        assertEquals(0, scheduleSequence.size)
    }
}
