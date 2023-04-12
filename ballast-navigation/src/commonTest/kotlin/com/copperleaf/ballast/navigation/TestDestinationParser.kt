package com.copperleaf.ballast.navigation

import com.copperleaf.ballast.navigation.Assertions.assertEquals
import com.copperleaf.ballast.navigation.Assertions.assertSame
import com.copperleaf.ballast.navigation.routing.UnmatchedDestination
import io.kotest.core.spec.style.StringSpec

class TestDestinationParser : StringSpec({

    "testParseDestination" {
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
})
