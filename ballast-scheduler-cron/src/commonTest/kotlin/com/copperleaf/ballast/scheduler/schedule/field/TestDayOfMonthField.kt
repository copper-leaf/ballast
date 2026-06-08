package com.copperleaf.ballast.scheduler.schedule.field

import com.copperleaf.ballast.scheduler.schedule.DayOfMonthField
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails

class TestDayOfMonthField {

    @Test
    fun testNonWildcardValueFactoryFunctions() {
        DayOfMonthField(1)
        DayOfMonthField(1, 2, 3)
        DayOfMonthField(listOf(1, 2))
        DayOfMonthField(1..31)
        DayOfMonthField(1..31 step 4)
        DayOfMonthField(31 downTo 1)
        DayOfMonthField(31 downTo 1 step 4)
    }

    @Test
    fun testWildcardValueFactoryFunctions() {
        DayOfMonthField(1, wildcard = true)
        DayOfMonthField(1, 2, 3, wildcard = true)
        DayOfMonthField(listOf(1, 2), true)
        DayOfMonthField(1..31, true)
        DayOfMonthField(1..31 step 4, true)
        DayOfMonthField(31 downTo 1, true)
        DayOfMonthField(31 downTo 1 step 4, true)
    }

    @Test
    fun testCronExpressionFactoryFunctions() {
        DayOfMonthField.anyValue().let {
            assertEquals(
                actual = it.values,
                expected = listOf(
                    1, 2, 3, 4, 5, 6, 7, 8, 9,
                    10, 11, 12, 13, 14, 15, 16, 17, 18, 19,
                    20, 21, 22, 23, 24, 25, 26, 27, 28, 29,
                    30, 31,
                )
            )
            assertEquals(
                actual = it.wildcard,
                expected = true,
            )
        }
        DayOfMonthField.anyValue(step = 15).let {
            assertEquals(
                actual = it.values,
                expected = listOf(1, 16, 31)
            )
            assertEquals(
                actual = it.wildcard,
                expected = true,
            )
        }
        DayOfMonthField.anyValue(step = 30).let {
            assertEquals(
                actual = it.values,
                expected = listOf(1, 31)
            )
            assertEquals(
                actual = it.wildcard,
                expected = true,
            )
        }
        DayOfMonthField.exactValue(30).let {
            assertEquals(
                actual = it.values,
                expected = listOf(30)
            )
            assertEquals(
                actual = it.wildcard,
                expected = false,
            )
        }
        DayOfMonthField.range(10, 20).let {
            assertEquals(
                actual = it.values,
                expected = listOf(10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20)
            )
            assertEquals(
                actual = it.wildcard,
                expected = false,
            )
        }
        DayOfMonthField.range(10, 20, step = 2).let {
            assertEquals(
                actual = it.values,
                expected = listOf(10, 12, 14, 16, 18, 20)
            )
            assertEquals(
                actual = it.wildcard,
                expected = false,
            )
        }
    }

    @Test
    fun testInvalidValueFactoryFunctions() {
        assertFails { DayOfMonthField(DayOfMonthField.MIN_VALUE - 1) }
        assertFails { DayOfMonthField(DayOfMonthField.MAX_VALUE + 1) }
    }
}
