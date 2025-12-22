package com.copperleaf.ballast.scheduler.schedule.value

import com.copperleaf.ballast.scheduler.schedule.ExactValue
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@Ignore
class ExactValueTest {

    @Test
    fun testMatches() {
        ExactValue(min = 0, max = 6, value = 0).let {
            assertFalse { it.matches(-1) } // out of range
            assertTrue { it.matches(0) }
            assertFalse { it.matches(1) }
            assertFalse { it.matches(2) }
            assertFalse { it.matches(3) }
            assertFalse { it.matches(4) }
            assertFalse { it.matches(5) }
            assertFalse { it.matches(6) }
            assertFalse { it.matches(7) } // out of range
        }

        ExactValue(min = 0, max = 6, value = 3).let {
            assertFalse { it.matches(-1) } // out of range
            assertFalse { it.matches(0) }
            assertFalse { it.matches(1) }
            assertFalse { it.matches(2) }
            assertTrue { it.matches(3) }
            assertFalse { it.matches(4) }
            assertFalse { it.matches(5) }
            assertFalse { it.matches(6) }
            assertFalse { it.matches(7) } // out of range
        }

        ExactValue(min = 0, max = 6, value = 6).let {
            assertFalse { it.matches(-1) } // out of range
            assertFalse { it.matches(0) }
            assertFalse { it.matches(1) }
            assertFalse { it.matches(2) }
            assertFalse { it.matches(3) }
            assertFalse { it.matches(4) }
            assertFalse { it.matches(5) }
            assertTrue { it.matches(6) }
            assertFalse { it.matches(7) } // out of range
        }
    }

    @Test
    fun testNextOrSame() {
        ExactValue(min = 0, max = 6, value = 0).let {
            assertEquals(actual = it.nextOrSame(-1), expected = null)
            assertEquals(actual = it.nextOrSame(0), expected = 0)
            assertEquals(actual = it.nextOrSame(1), expected = null)
            assertEquals(actual = it.nextOrSame(2), expected = null)
            assertEquals(actual = it.nextOrSame(3), expected = null)
            assertEquals(actual = it.nextOrSame(4), expected = null)
            assertEquals(actual = it.nextOrSame(5), expected = null)
            assertEquals(actual = it.nextOrSame(6), expected = null)
            assertEquals(actual = it.nextOrSame(7), expected = null)
        }

        ExactValue(min = 0, max = 6, value = 3).let {
            assertEquals(actual = it.nextOrSame(-1), expected = null)
            assertEquals(actual = it.nextOrSame(0), expected = 3)
            assertEquals(actual = it.nextOrSame(1), expected = 3)
            assertEquals(actual = it.nextOrSame(2), expected = 3)
            assertEquals(actual = it.nextOrSame(3), expected = 3)
            assertEquals(actual = it.nextOrSame(4), expected = null)
            assertEquals(actual = it.nextOrSame(5), expected = null)
            assertEquals(actual = it.nextOrSame(6), expected = null)
            assertEquals(actual = it.nextOrSame(7), expected = null)
        }

        ExactValue(min = 0, max = 6, value = 6).let {
            assertEquals(actual = it.nextOrSame(-1), expected = null)
            assertEquals(actual = it.nextOrSame(0), expected = 6)
            assertEquals(actual = it.nextOrSame(1), expected = 6)
            assertEquals(actual = it.nextOrSame(2), expected = 6)
            assertEquals(actual = it.nextOrSame(3), expected = 6)
            assertEquals(actual = it.nextOrSame(4), expected = 6)
            assertEquals(actual = it.nextOrSame(5), expected = 6)
            assertEquals(actual = it.nextOrSame(6), expected = 6)
            assertEquals(actual = it.nextOrSame(7), expected = null)
        }
    }
}
