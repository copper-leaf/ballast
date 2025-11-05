package com.copperleaf.ballast.navigation

import com.copperleaf.ballast.navigation.routing.UnmatchedDestination
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

class TestDestinationParser {

    @Test
    fun testParseDestination() = runTest {
        fun String.parseDestination() = UnmatchedDestination.parse(this)

        "/".let { original ->
            original.parseDestination().apply {
                assertSame(originalDestinationUrl, original)
                assertEquals(listOf(), matchablePathSegments)
                assertEquals(emptyMap(), matchableQueryParameters)
            }
        }
        "/one".let { original ->
            original.parseDestination().apply {
                assertSame(originalDestinationUrl, original)
                assertEquals(listOf("one"), matchablePathSegments)
                assertEquals(emptyMap(), matchableQueryParameters)
            }
        }
        "/one/two".let { original ->
            original.parseDestination().apply {
                assertSame(originalDestinationUrl, original)
                assertEquals(listOf("one", "two"), matchablePathSegments)
                assertEquals(emptyMap(), matchableQueryParameters)
            }
        }
        "/one/two?asdf=123&qwerty=456".let { original ->
            original.parseDestination().apply {
                assertSame(originalDestinationUrl, original)
                assertEquals(listOf("one", "two"), matchablePathSegments)
                assertEquals(mapOf("asdf" to listOf("123"), "qwerty" to listOf("456")), matchableQueryParameters)
            }
        }
    }
}
