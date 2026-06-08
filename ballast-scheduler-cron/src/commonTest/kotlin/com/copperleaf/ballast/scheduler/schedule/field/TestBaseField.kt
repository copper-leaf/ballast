package com.copperleaf.ballast.scheduler.schedule.field

import com.copperleaf.ballast.scheduler.schedule.MonthField
import kotlinx.datetime.Month
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TestBaseField {

    @Test
    fun testConstructor_ListInt() {
        MonthField(listOf(1, 2))
        assertFails { MonthField(emptyList<Int>()) }
        assertFails { MonthField(listOf(0)) }
        assertFails { MonthField(listOf(13)) }
    }

    @Test
    fun testConstructor_VarargInt() {
        MonthField(1, 2)
        assertFails { MonthField(0) }
        assertFails { MonthField(13) }
    }

    @Test
    fun testConstructor_IntRange() {
        MonthField(1..2)
        MonthField(1 until 2)
        assertFails { MonthField(0..2) }
        assertFails { MonthField(1..13) }
    }

    @Test
    fun testConstructor_IntProgression() {
        MonthField(1..12 step 4)
        MonthField(1 until 12 step 4)
        assertFails { MonthField(0..2 step 1) }
        assertFails { MonthField(1..13 step 2) }
    }

    @Test
    fun testConstructor_ListMonth() {
        MonthField(listOf(Month.JANUARY, Month.FEBRUARY))
        assertFails { MonthField(emptyList<Month>()) }
    }

    @Test
    fun testConstructor_VarargMonth() {
        MonthField(Month.JANUARY, Month.FEBRUARY)
    }

    @Test
    fun testConstructor_MonthEnumEntries() {
        MonthField(Month.entries)
    }

    @Test
    fun testMatches() {
        MonthField(listOf(1, 2)).apply {
            assertFalse { matches(0) }
            assertTrue { matches(1) }
            assertTrue { matches(2) }
            assertFalse { matches(3) }
            assertFalse { matches(4) }
            assertFalse { matches(5) }
            assertFalse { matches(6) }
            assertFalse { matches(7) }
            assertFalse { matches(8) }
            assertFalse { matches(9) }
            assertFalse { matches(10) }
            assertFalse { matches(11) }
            assertFalse { matches(12) }
            assertFalse { matches(13) }
        }
        MonthField(listOf(1, 4)).apply {
            assertFalse { matches(0) }
            assertTrue { matches(1) }
            assertFalse { matches(2) }
            assertFalse { matches(3) }
            assertTrue { matches(4) }
            assertFalse { matches(5) }
            assertFalse { matches(6) }
            assertFalse { matches(7) }
            assertFalse { matches(8) }
            assertFalse { matches(9) }
            assertFalse { matches(10) }
            assertFalse { matches(11) }
            assertFalse { matches(12) }
            assertFalse { matches(13) }
        }
        MonthField(listOf(1, 2, 3, 4)).apply {
            assertFalse { matches(0) }
            assertTrue { matches(1) }
            assertTrue { matches(2) }
            assertTrue { matches(3) }
            assertTrue { matches(4) }
            assertFalse { matches(5) }
            assertFalse { matches(6) }
            assertFalse { matches(7) }
            assertFalse { matches(8) }
            assertFalse { matches(9) }
            assertFalse { matches(10) }
            assertFalse { matches(11) }
            assertFalse { matches(12) }
            assertFalse { matches(13) }
        }
    }

    @Test
    fun testNextOrSame() {
        MonthField(listOf(1, 2)).apply {
            assertEquals(null, nextOrSame(0))
            assertEquals(1, nextOrSame(1))
            assertEquals(2, nextOrSame(2))
            assertEquals(null, nextOrSame(3))
            assertEquals(null, nextOrSame(4))
            assertEquals(null, nextOrSame(5))
            assertEquals(null, nextOrSame(6))
            assertEquals(null, nextOrSame(7))
            assertEquals(null, nextOrSame(8))
            assertEquals(null, nextOrSame(9))
            assertEquals(null, nextOrSame(10))
            assertEquals(null, nextOrSame(11))
            assertEquals(null, nextOrSame(12))
            assertEquals(null, nextOrSame(13))
        }
        MonthField(listOf(1, 4)).apply {
            assertEquals(null, nextOrSame(0))
            assertEquals(1, nextOrSame(1))
            assertEquals(4, nextOrSame(2))
            assertEquals(4, nextOrSame(3))
            assertEquals(4, nextOrSame(4))
            assertEquals(null, nextOrSame(5))
            assertEquals(null, nextOrSame(6))
            assertEquals(null, nextOrSame(7))
            assertEquals(null, nextOrSame(8))
            assertEquals(null, nextOrSame(9))
            assertEquals(null, nextOrSame(10))
            assertEquals(null, nextOrSame(11))
            assertEquals(null, nextOrSame(12))
            assertEquals(null, nextOrSame(13))
        }
        MonthField(listOf(1, 2, 3, 4)).apply {
            assertEquals(null, nextOrSame(0))
            assertEquals(1, nextOrSame(1))
            assertEquals(2, nextOrSame(2))
            assertEquals(3, nextOrSame(3))
            assertEquals(4, nextOrSame(4))
            assertEquals(null, nextOrSame(5))
            assertEquals(null, nextOrSame(6))
            assertEquals(null, nextOrSame(7))
            assertEquals(null, nextOrSame(8))
            assertEquals(null, nextOrSame(9))
            assertEquals(null, nextOrSame(10))
            assertEquals(null, nextOrSame(11))
            assertEquals(null, nextOrSame(12))
            assertEquals(null, nextOrSame(13))
        }
    }
}
