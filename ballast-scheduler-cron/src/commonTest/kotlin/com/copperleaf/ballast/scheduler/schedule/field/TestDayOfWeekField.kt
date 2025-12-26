package com.copperleaf.ballast.scheduler.schedule.field

import com.copperleaf.ballast.scheduler.schedule.DayOfWeekField
import kotlinx.datetime.DayOfWeek
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails

class TestDayOfWeekField {

    @Test
    fun testNonWildcardValueFactoryFunctions() {
        DayOfWeekField(1)
        DayOfWeekField(1, 2, 3)
        DayOfWeekField(listOf(1, 2))
        DayOfWeekField(0..6)
        DayOfWeekField(0..6 step 4)
        DayOfWeekField(6 downTo 0)
        DayOfWeekField(6 downTo 0 step 4)
        DayOfWeekField(DayOfWeek.SUNDAY)
        DayOfWeekField(DayOfWeek.SUNDAY, DayOfWeek.MONDAY)
        DayOfWeekField(listOf(DayOfWeek.SUNDAY, DayOfWeek.MONDAY))
        DayOfWeekField(DayOfWeek.entries)
    }

    @Test
    fun testWildcardValueFactoryFunctions() {
        DayOfWeekField(1, wildcard = true)
        DayOfWeekField(1, 2, 3, wildcard = true)
        DayOfWeekField(listOf(1, 2), true)
        DayOfWeekField(0..6, true)
        DayOfWeekField(0..6 step 4, true)
        DayOfWeekField(6 downTo 0, true)
        DayOfWeekField(6 downTo 0 step 4, true)
        DayOfWeekField(DayOfWeek.SUNDAY, wildcard = true)
        DayOfWeekField(DayOfWeek.SUNDAY, DayOfWeek.MONDAY, wildcard = true)
        DayOfWeekField(listOf(DayOfWeek.SUNDAY, DayOfWeek.MONDAY), true)
        DayOfWeekField(DayOfWeek.entries, true)
    }

    @Test
    fun testCronExpressionFactoryFunctions() {
        DayOfWeekField.anyValue().let {
            assertEquals(
                actual = it.values,
                expected = listOf(0, 1, 2, 3, 4, 5, 6)
            )
            assertEquals(
                actual = it.wildcard,
                expected = true,
            )
        }
        DayOfWeekField.anyValue(step = 2).let {
            assertEquals(
                actual = it.values,
                expected = listOf(0, 2, 4, 6)
            )
            assertEquals(
                actual = it.wildcard,
                expected = true,
            )
        }
        DayOfWeekField.anyValue(step = 5).let {
            assertEquals(
                actual = it.values,
                expected = listOf(0, 5)
            )
            assertEquals(
                actual = it.wildcard,
                expected = true,
            )
        }
        DayOfWeekField.exactValue(4).let {
            assertEquals(
                actual = it.values,
                expected = listOf(4)
            )
            assertEquals(
                actual = it.wildcard,
                expected = false,
            )
        }
        DayOfWeekField.range(2, 5).let {
            assertEquals(
                actual = it.values,
                expected = listOf(2, 3, 4, 5)
            )
            assertEquals(
                actual = it.wildcard,
                expected = false,
            )
        }
        DayOfWeekField.range(2, 5, step = 2).let {
            assertEquals(
                actual = it.values,
                expected = listOf(2, 4)
            )
            assertEquals(
                actual = it.wildcard,
                expected = false,
            )
        }
    }

    @Test
    fun testInvalidValueFactoryFunctions() {
        assertFails { DayOfWeekField(DayOfWeekField.MIN_VALUE - 1) }
        assertFails { DayOfWeekField(DayOfWeekField.MAX_VALUE + 1) }
    }
}
