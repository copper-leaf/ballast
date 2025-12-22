package com.copperleaf.ballast.scheduler.schedule.value

import com.copperleaf.ballast.scheduler.schedule.AnyValue
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AnyValueTest {

    @Test
    fun testMatches() {
        AnyValue(min = 0, max = 6, step = 1).let {
            assertFalse { it.matches(-1) } // out of range
            assertTrue { it.matches(0) }
            assertTrue { it.matches(1) }
            assertTrue { it.matches(2) }
            assertTrue { it.matches(3) }
            assertTrue { it.matches(4) }
            assertTrue { it.matches(5) }
            assertTrue { it.matches(6) }
            assertFalse { it.matches(7) } // out of range
        }

        AnyValue(min = 0, max = 6, step = 2).let {
            assertFalse { it.matches(-1) } // out of range
            assertTrue { it.matches(0) }
            assertFalse { it.matches(1) } // not in step
            assertTrue { it.matches(2) }
            assertFalse { it.matches(3) } // not in step
            assertTrue { it.matches(4) }
            assertFalse { it.matches(5) } // not in step
            assertTrue { it.matches(6) }
            assertFalse { it.matches(7) } // out of range
        }

        AnyValue(min = 0, max = 6, step = 3).let {
            assertFalse { it.matches(-1) } // out of range
            assertTrue { it.matches(0) }
            assertFalse { it.matches(1) } // not in step
            assertFalse { it.matches(2) } // not in step
            assertTrue { it.matches(3) }
            assertFalse { it.matches(4) } // not in step
            assertFalse { it.matches(5) } // not in step
            assertTrue { it.matches(6) }
            assertFalse { it.matches(7) } // out of range
        }
    }

    @Test
    fun testNextOrSame() {
        AnyValue(min = 0, max = 6, step = 1).let {
            assertEquals(actual = it.nextOrSame(-1), expected = null)
            assertEquals(actual = it.nextOrSame(0), expected = 0)
            assertEquals(actual = it.nextOrSame(1), expected = 1)
            assertEquals(actual = it.nextOrSame(2), expected = 2)
            assertEquals(actual = it.nextOrSame(3), expected = 3)
            assertEquals(actual = it.nextOrSame(4), expected = 4)
            assertEquals(actual = it.nextOrSame(5), expected = 5)
            assertEquals(actual = it.nextOrSame(6), expected = 6)
            assertEquals(actual = it.nextOrSame(7), expected = null)
        }

        AnyValue(min = 0, max = 6, step = 2).let {
            assertEquals(actual = it.nextOrSame(-1), expected = null)
            assertEquals(actual = it.nextOrSame(0), expected = 0)
            assertEquals(actual = it.nextOrSame(1), expected = 2)
            assertEquals(actual = it.nextOrSame(2), expected = 2)
            assertEquals(actual = it.nextOrSame(3), expected = 4)
            assertEquals(actual = it.nextOrSame(4), expected = 4)
            assertEquals(actual = it.nextOrSame(5), expected = 6)
            assertEquals(actual = it.nextOrSame(6), expected = 6)
            assertEquals(actual = it.nextOrSame(7), expected = null)
        }

        AnyValue(min = 0, max = 6, step = 3).let {
            assertEquals(actual = it.nextOrSame(-1), expected = null)
            assertEquals(actual = it.nextOrSame(0), expected = 0)
            assertEquals(actual = it.nextOrSame(1), expected = 3)
            assertEquals(actual = it.nextOrSame(2), expected = 3)
            assertEquals(actual = it.nextOrSame(3), expected = 3)
            assertEquals(actual = it.nextOrSame(4), expected = 6)
            assertEquals(actual = it.nextOrSame(5), expected = 6)
            assertEquals(actual = it.nextOrSame(6), expected = 6)
            assertEquals(actual = it.nextOrSame(7), expected = null)
        }
    }
}
