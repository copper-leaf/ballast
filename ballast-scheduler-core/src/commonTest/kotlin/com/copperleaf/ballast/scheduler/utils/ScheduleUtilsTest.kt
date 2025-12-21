package com.copperleaf.ballast.scheduler.utils

import com.copperleaf.ballast.scheduler.UnsafeFixedInstantSchedule
import com.copperleaf.ballast.scheduler.firstTen
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlin.test.Test
import kotlin.test.assertFails
import kotlin.time.Duration.Companion.seconds

class ScheduleUtilsTest {

    val timeZone = TimeZone.UTC
    val startDay = LocalDate(2023, Month.DECEMBER, 28)
    val startInstant = startDay.atStartOfDayIn(timeZone)

    @Test
    fun generateSafeSchedule_beforeStartValue_throws() = runTest {
        assertFails {
            UnsafeFixedInstantSchedule(
                startInstant.minus(1.seconds),
            )
                .generateSafeSchedule(startInstant)
                .firstTen()
        }
    }

    @Test
    fun generateSafeSchedule_sameAsStartValue_throws() = runTest {
        assertFails {
            UnsafeFixedInstantSchedule(
                startInstant,
            )
                .generateSafeSchedule(startInstant)
                .firstTen()
        }
    }

    @Test
    fun generateSafeSchedule_afterStartValue_doesNotThrow() = runTest {
        UnsafeFixedInstantSchedule(
            startInstant.plus(1.seconds),
        )
            .generateSafeSchedule(startInstant)
            .firstTen()
    }

    @Test
    fun generateSafeSchedule_beforePreviousValueDoes_throws() = runTest {
        assertFails {
            UnsafeFixedInstantSchedule(
                startInstant.plus(5.seconds),
                startInstant.plus(4.seconds),
            )
                .generateSafeSchedule(startInstant)
                .firstTen()
        }
    }

    @Test
    fun generateSafeSchedule_sameAsPreviousValueDoes_throws() = runTest {
        assertFails {
            UnsafeFixedInstantSchedule(
                startInstant.plus(5.seconds),
                startInstant.plus(5.seconds),
            )
                .generateSafeSchedule(startInstant)
                .firstTen()
        }
    }

    @Test
    fun generateSafeSchedule_afterPreviousValue_doesNotThrow() = runTest {
        UnsafeFixedInstantSchedule(
            startInstant.plus(5.seconds),
            startInstant.plus(6.seconds),
        )
            .generateSafeSchedule(startInstant)
            .firstTen()
    }
}
