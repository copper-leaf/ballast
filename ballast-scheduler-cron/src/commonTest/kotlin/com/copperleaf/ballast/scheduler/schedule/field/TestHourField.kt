package com.copperleaf.ballast.scheduler.schedule.field

import com.copperleaf.ballast.scheduler.schedule.HourField
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails

class TestHourField {

    @Test
    fun testNonWildcardValueFactoryFunctions() {
        HourField(1)
        HourField(1, 2, 3)
        HourField(listOf(1, 2))
        HourField(0..23)
        HourField(0..23 step 4)
        HourField(23 downTo 1)
        HourField(23 downTo 1 step 4)
    }

    @Test
    fun testWildcardValueFactoryFunctions() {
        HourField(1, wildcard = true)
        HourField(1, 2, 3, wildcard = true)
        HourField(listOf(1, 2), true)
        HourField(0..23, true)
        HourField(0..23 step 4, true)
        HourField(23 downTo 1, true)
        HourField(23 downTo 1 step 4, true)
    }

    @Test
    fun testCronExpressionFactoryFunctions() {
        HourField.anyValue().let {
            assertEquals(
                actual = it.values,
                expected = listOf(
                    0, 1, 2, 3, 4, 5, 6, 7, 8, 9,
                    10, 11, 12, 13, 14, 15, 16, 17, 18, 19,
                    20, 21, 22, 23,
                )
            )
            assertEquals(
                actual = it.wildcard,
                expected = true,
            )
        }
        HourField.anyValue(step = 15).let {
            assertEquals(
                actual = it.values,
                expected = listOf(0, 15)
            )
            assertEquals(
                actual = it.wildcard,
                expected = true,
            )
        }
        HourField.anyValue(step = 4).let {
            assertEquals(
                actual = it.values,
                expected = listOf(0, 4, 8, 12, 16, 20)
            )
            assertEquals(
                actual = it.wildcard,
                expected = true,
            )
        }
        HourField.exactValue(20).let {
            assertEquals(
                actual = it.values,
                expected = listOf(20)
            )
            assertEquals(
                actual = it.wildcard,
                expected = false,
            )
        }
        HourField.range(10, 20).let {
            assertEquals(
                actual = it.values,
                expected = listOf(10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20)
            )
            assertEquals(
                actual = it.wildcard,
                expected = false,
            )
        }
        HourField.range(10, 20, step = 2).let {
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
        assertFails { HourField(HourField.MIN_VALUE - 1) }
        assertFails { HourField(HourField.MAX_VALUE + 1) }
    }
}
