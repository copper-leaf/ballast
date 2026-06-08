package com.copperleaf.ballast.scheduler.schedule.field

import com.copperleaf.ballast.scheduler.schedule.MinuteField
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails

class TestMinuteField {

    @Test
    fun testNonWildcardValueFactoryFunctions() {
        MinuteField(1)
        MinuteField(1, 2, 3)
        MinuteField(listOf(1, 2))
        MinuteField(1..12)
        MinuteField(1..12 step 4)
        MinuteField(12 downTo 1)
        MinuteField(12 downTo 1 step 4)
    }

    @Test
    fun testWildcardValueFactoryFunctions() {
        MinuteField(1, wildcard = true)
        MinuteField(1, 2, 3, wildcard = true)
        MinuteField(listOf(1, 2), true)
        MinuteField(1..12, true)
        MinuteField(1..12 step 4, true)
        MinuteField(12 downTo 1, true)
        MinuteField(12 downTo 1 step 4, true)
    }

    @Test
    fun testCronExpressionFactoryFunctions() {
        MinuteField.anyValue().let {
            assertEquals(
                actual = it.values,
                expected = listOf(
                    0, 1, 2, 3, 4, 5, 6, 7, 8, 9,
                    10, 11, 12, 13, 14, 15, 16, 17, 18, 19,
                    20, 21, 22, 23, 24, 25, 26, 27, 28, 29,
                    30, 31, 32, 33, 34, 35, 36, 37, 38, 39,
                    40, 41, 42, 43, 44, 45, 46, 47, 48, 49,
                    50, 51, 52, 53, 54, 55, 56, 57, 58, 59,
                )
            )
            assertEquals(
                actual = it.wildcard,
                expected = true,
            )
        }
        MinuteField.anyValue(step = 15).let {
            assertEquals(
                actual = it.values,
                expected = listOf(0, 15, 30, 45)
            )
            assertEquals(
                actual = it.wildcard,
                expected = true,
            )
        }
        MinuteField.anyValue(step = 30).let {
            assertEquals(
                actual = it.values,
                expected = listOf(0, 30)
            )
            assertEquals(
                actual = it.wildcard,
                expected = true,
            )
        }
        MinuteField.exactValue(30).let {
            assertEquals(
                actual = it.values,
                expected = listOf(30)
            )
            assertEquals(
                actual = it.wildcard,
                expected = false,
            )
        }
        MinuteField.range(10, 20).let {
            assertEquals(
                actual = it.values,
                expected = listOf(10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20)
            )
            assertEquals(
                actual = it.wildcard,
                expected = false,
            )
        }
        MinuteField.range(10, 20, step = 2).let {
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
    fun testInvalidFactoryFunctions() {
        assertFails { MinuteField(MinuteField.MIN_VALUE - 1) }
        assertFails { MinuteField(MinuteField.MAX_VALUE + 1) }
    }
}
