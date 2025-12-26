package com.copperleaf.ballast.scheduler.schedule.field

import com.copperleaf.ballast.scheduler.schedule.MonthField
import kotlinx.datetime.Month
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails

class TestMonthField {

    @Test
    fun testNonWildcardValueFactoryFunctions() {
        MonthField(1)
        MonthField(1, 2, 3)
        MonthField(listOf(1, 2))
        MonthField(1..12)
        MonthField(1..12 step 4)
        MonthField(12 downTo 1)
        MonthField(12 downTo 1 step 4)
        MonthField(Month.JANUARY)
        MonthField(Month.JANUARY, Month.FEBRUARY)
        MonthField(listOf(Month.JANUARY, Month.FEBRUARY))
        MonthField(Month.entries)
    }

    @Test
    fun testWildcardValueFactoryFunctions() {
        MonthField(1, wildcard = true)
        MonthField(1, 2, 3, wildcard = true)
        MonthField(listOf(1, 2), true)
        MonthField(1..12, true)
        MonthField(1..12 step 4, true)
        MonthField(12 downTo 1, true)
        MonthField(12 downTo 1 step 4, true)
        MonthField(Month.JANUARY, wildcard = true)
        MonthField(Month.JANUARY, Month.FEBRUARY, wildcard = true)
        MonthField(listOf(Month.JANUARY, Month.FEBRUARY), true)
        MonthField(Month.entries, true)
    }

    @Test
    fun testCronExpressionFactoryFunctions() {
        MonthField.anyValue().let {
            assertEquals(
                actual = it.values,
                expected = listOf(
                    1, 2, 3, 4, 5, 6, 7, 8, 9,
                    10, 11, 12
                )
            )
            assertEquals(
                actual = it.wildcard,
                expected = true,
            )
        }
        MonthField.anyValue(step = 4).let {
            assertEquals(
                actual = it.values,
                expected = listOf(1, 5, 9)
            )
            assertEquals(
                actual = it.wildcard,
                expected = true,
            )
        }
        MonthField.anyValue(step = 6).let {
            assertEquals(
                actual = it.values,
                expected = listOf(1, 7)
            )
            assertEquals(
                actual = it.wildcard,
                expected = true,
            )
        }
        MonthField.exactValue(8).let {
            assertEquals(
                actual = it.values,
                expected = listOf(8)
            )
            assertEquals(
                actual = it.wildcard,
                expected = false,
            )
        }
        MonthField.range(4, 12).let {
            assertEquals(
                actual = it.values,
                expected = listOf(4, 5, 6, 7, 8, 9, 10, 11, 12)
            )
            assertEquals(
                actual = it.wildcard,
                expected = false,
            )
        }
        MonthField.range(4, 12, step = 2).let {
            assertEquals(
                actual = it.values,
                expected = listOf(4, 6, 8, 10, 12)
            )
            assertEquals(
                actual = it.wildcard,
                expected = false,
            )
        }
    }

    @Test
    fun testInvalidValueFactoryFunctions() {
        assertFails { MonthField(MonthField.MIN_VALUE - 1) }
        assertFails { MonthField(MonthField.MAX_VALUE + 1) }
    }
}
