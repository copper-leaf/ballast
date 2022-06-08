package com.copperleaf.ballast.navigation

import com.copperleaf.ballast.navigation.routing.NavGraph
import com.copperleaf.ballast.navigation.routing.PathSegment
import com.copperleaf.ballast.navigation.routing.Route
import com.copperleaf.ballast.navigation.routing.createMatcher
import com.copperleaf.ballast.navigation.routing.findMatch
import com.copperleaf.ballast.navigation.routing.matchDestinationOrThrow
import com.copperleaf.ballast.navigation.routing.parseDestination
import com.copperleaf.ballast.navigation.routing.toPathSegment
import com.copperleaf.ballast.navigation.routing.weight
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertSame

class ParserTest {

    @Test
    fun testParseDestination() {
        "/".let { original ->
            original.parseDestination().apply {
                assertSame(originalUrl, original)
                assertEquals("/", path)
                assertEquals(listOf(), pathSegments)
                assertEquals(emptyMap(), queryParameters)
            }
        }
        "/one".let { original ->
            original.parseDestination().apply {
                assertSame(originalUrl, original)
                assertEquals("/one", path)
                assertEquals(listOf("one"), pathSegments)
                assertEquals(emptyMap(), queryParameters)
            }
        }
        "/one/two".let { original ->
            original.parseDestination().apply {
                assertSame(originalUrl, original)
                assertEquals("/one/two", path)
                assertEquals(listOf("one", "two"), pathSegments)
                assertEquals(emptyMap(), queryParameters)
            }
        }
        "/one/two?asdf=123&qwerty=456".let { original ->
            original.parseDestination().apply {
                assertSame(originalUrl, original)
                assertEquals("/one/two", path)
                assertEquals(listOf("one", "two"), pathSegments)
                assertEquals(mapOf("asdf" to listOf("123"), "qwerty" to listOf("456")), queryParameters)
            }
        }
    }

    @Test
    fun testToPathSegment() {
        "one".toPathSegment().apply {
            assertEquals(PathSegment.Static("one"), this)
            assertEquals(1, weight)
        }
        "two".toPathSegment().apply {
            assertEquals(PathSegment.Static("two"), this)
            assertEquals(1, weight)
        }
        ":one".toPathSegment().apply {
            assertEquals(PathSegment.Parameter("one", false), this)
            assertEquals(2, weight)
        }
        "{one}".toPathSegment().apply {
            assertEquals(PathSegment.Parameter("one", false), this)
            assertEquals(2, weight)
        }
        "*".toPathSegment().apply {
            assertEquals(PathSegment.Wildcard, this)
            assertEquals(3, weight)
        }
        "{one?}".toPathSegment().apply {
            assertEquals(PathSegment.Parameter("one", true), this)
            assertEquals(4, weight)
        }
        "{...}".toPathSegment().apply {
            assertEquals(PathSegment.Tailcard(null), this)
            assertEquals(5, weight)
        }
        "{one...}".toPathSegment().apply {
            assertEquals(PathSegment.Tailcard("one"), this)
            assertEquals(5, weight)
        }
    }

    @Test
    fun testCreateMatcher() {
        "/one".createMatcher().apply {
            assertEquals(
                listOf(
                    PathSegment.Static("one"),
                ),
                path
            )
        }
        "/one/two".createMatcher().apply {
            assertEquals(
                listOf(
                    PathSegment.Static("one"),
                    PathSegment.Static("two"),
                ),
                path
            )
        }
        "/one/:two".createMatcher().apply {
            assertEquals(
                listOf(
                    PathSegment.Static("one"),
                    PathSegment.Parameter("two", false),
                ),
                path
            )
        }
        "/one/:two/{three?}".createMatcher().apply {
            assertEquals(
                listOf(
                    PathSegment.Static("one"),
                    PathSegment.Parameter("two", false),
                    PathSegment.Parameter("three", true),
                ),
                path
            )
        }
        "/one/:two/{three}/*/{four}".createMatcher().apply {
            assertEquals(
                listOf(
                    PathSegment.Static("one"),
                    PathSegment.Parameter("two", false),
                    PathSegment.Parameter("three", false),
                    PathSegment.Wildcard,
                    PathSegment.Parameter("four", false),
                ),
                path
            )
        }

        assertFailsWith<Throwable> { "/one/:two/{three?}/{four?}".createMatcher() }.let {
            assertEquals("you can only have one optional parameter or tailcard, but not both", it.message)
        }
        assertFailsWith<Throwable> { "/one/:two/{three?}/{...}".createMatcher() }.let {
            assertEquals("you can only have one optional parameter or tailcard, but not both", it.message)
        }
        assertFailsWith<Throwable> { "/one/:two/{three?}/{four...}".createMatcher() }.let {
            assertEquals("you can only have one optional parameter or tailcard, but not both", it.message)
        }
        assertFailsWith<Throwable> { "/one/:two/{three?}/four".createMatcher() }.let {
            assertEquals("optional parameters and tailcards must be at the end of the path", it.message)
        }
        assertFailsWith<Throwable> { "/one/:two/{two}".createMatcher() }.let {
            assertEquals("parameter names must be unique", it.message)
        }
        assertFailsWith<Throwable> { "/one/:two/{two...}".createMatcher() }.let {
            assertEquals("parameter names must be unique", it.message)
        }
    }

    @Test
    fun testMatchDestinationOrThrow() {
        Route("/one").let { route ->
            assertEquals(1.0, route.matcher.weight)

            route.matchDestinationOrThrow("/one").let { destination ->
                assertEquals("/one", destination.path)
                assertEquals(listOf("one"), destination.pathSegments)
                assertEquals(emptyMap(), destination.pathParameters)
                assertEquals(emptyMap(), destination.queryParameters)
            }
            route.matchDestinationOrThrow("/one?asdf=123&qwerty=456").let { destination ->
                assertEquals("/one", destination.path)
                assertEquals(listOf("one"), destination.pathSegments)
                assertEquals(emptyMap(), destination.pathParameters)
                assertEquals(mapOf("asdf" to listOf("123"), "qwerty" to listOf("456")), destination.queryParameters)
            }
        }

        Route("/one/:two").let { route ->
            assertEquals(12.0, route.matcher.weight)

            assertFailsWith<Throwable> { route.matchDestinationOrThrow("/one") }.apply {
                assertEquals("Destination (/one) does not match Route (/one/:two)", message)
            }
            assertFailsWith<Throwable> { route.matchDestinationOrThrow("/one/two/three") }.apply {
                assertEquals("Destination (/one/two/three) does not match Route (/one/:two)", message)
            }
            route.matchDestinationOrThrow("/one/blah").let { destination ->
                assertEquals("/one/blah", destination.path)
                assertEquals(listOf("one", "blah"), destination.pathSegments)
                assertEquals(mapOf("two" to listOf("blah")), destination.pathParameters)
                assertEquals(emptyMap(), destination.queryParameters)
            }
            route.matchDestinationOrThrow("/one/blah?asdf=123&qwerty=456").let { destination ->
                assertEquals("/one/blah", destination.path)
                assertEquals(listOf("one", "blah"), destination.pathSegments)
                assertEquals(mapOf("two" to listOf("blah")), destination.pathParameters)
                assertEquals(mapOf("asdf" to listOf("123"), "qwerty" to listOf("456")), destination.queryParameters)
            }
        }

        Route("/one/{two?}").let { route ->
            assertEquals(14.0, route.matcher.weight)

            route.matchDestinationOrThrow("/one").let { destination ->
                assertEquals("/one", destination.path)
                assertEquals(listOf("one"), destination.pathSegments)
                assertEquals(emptyMap(), destination.pathParameters)
                assertEquals(emptyMap(), destination.queryParameters)
            }
            assertFailsWith<Throwable> { route.matchDestinationOrThrow("/one/two/three") }.apply {
                assertEquals("Destination (/one/two/three) does not match Route (/one/{two?})", message)
            }
            route.matchDestinationOrThrow("/one/blah").let { destination ->
                assertEquals("/one/blah", destination.path)
                assertEquals(listOf("one", "blah"), destination.pathSegments)
                assertEquals(mapOf("two" to listOf("blah")), destination.pathParameters)
                assertEquals(emptyMap(), destination.queryParameters)
            }
            route.matchDestinationOrThrow("/one/blah?asdf=123&qwerty=456").let { destination ->
                assertEquals("/one/blah", destination.path)
                assertEquals(listOf("one", "blah"), destination.pathSegments)
                assertEquals(mapOf("two" to listOf("blah")), destination.pathParameters)
                assertEquals(mapOf("asdf" to listOf("123"), "qwerty" to listOf("456")), destination.queryParameters)
            }
        }

        Route("/one/*/:two/{three...}").let { route ->
            assertEquals(1325.0, route.matcher.weight)

            route.matchDestinationOrThrow("/one/a/b").let { destination ->
                assertEquals("/one/a/b", destination.path)
                assertEquals(listOf("one", "a", "b"), destination.pathSegments)
                assertEquals(mapOf("two" to listOf("b"), "three" to emptyList()), destination.pathParameters)
                assertEquals(emptyMap(), destination.queryParameters)
            }
            route.matchDestinationOrThrow("/one/a/b/c").let { destination ->
                assertEquals("/one/a/b/c", destination.path)
                assertEquals(listOf("one", "a", "b", "c"), destination.pathSegments)
                assertEquals(mapOf("two" to listOf("b"), "three" to listOf("c")), destination.pathParameters)
                assertEquals(emptyMap(), destination.queryParameters)
            }
            route.matchDestinationOrThrow("/one/a/b/c/d").let { destination ->
                assertEquals("/one/a/b/c/d", destination.path)
                assertEquals(listOf("one", "a", "b", "c", "d"), destination.pathSegments)
                assertEquals(mapOf("two" to listOf("b"), "three" to listOf("c", "d")), destination.pathParameters)
                assertEquals(emptyMap(), destination.queryParameters)
            }
            route.matchDestinationOrThrow("/one/a/b/c/d/e/f/g/h/i").let { destination ->
                assertEquals("/one/a/b/c/d/e/f/g/h/i", destination.path)
                assertEquals(listOf("one", "a", "b", "c", "d", "e", "f", "g", "h", "i"), destination.pathSegments)
                assertEquals(mapOf("two" to listOf("b"), "three" to listOf("c", "d", "e", "f", "g", "h", "i")), destination.pathParameters)
                assertEquals(emptyMap(), destination.queryParameters)
            }
            route.matchDestinationOrThrow("/one/a/b/c/d/e/f/g/h/i?asdf=123&qwerty=456").let { destination ->
                assertEquals("/one/a/b/c/d/e/f/g/h/i", destination.path)
                assertEquals(listOf("one", "a", "b", "c", "d", "e", "f", "g", "h", "i"), destination.pathSegments)
                assertEquals(mapOf("two" to listOf("b"), "three" to listOf("c", "d", "e", "f", "g", "h", "i")), destination.pathParameters)
                assertEquals(mapOf("asdf" to listOf("123"), "qwerty" to listOf("456")), destination.queryParameters)
            }
        }
    }

    @Test
    fun testNavGraph() {
        val Home = Route("/app/home")
        val VerseOfTheDay = Route("/app/votd")
        val List = Route("/app/verses")
        val Detail = Route("/app/verses/{verseId}")
        val Create = Route("/app/verses/new")
        val EditVerse = Route("/app/verses/{verseId}/edit")

        val allRoutes = listOf(Home, VerseOfTheDay, List, Create, Detail, EditVerse)
        val allRoutesInOrderOfWeight = listOf(Home, VerseOfTheDay, List, Create, Detail, EditVerse)

        NavGraph(allRoutes).let { navGraph ->
            assertEquals(allRoutesInOrderOfWeight, navGraph.routes)

            navGraph.findMatch("/app/home").let {
                assertNotNull(it)
                assertEquals(Home, it.originalRoute)
            }
            navGraph.findMatch("/app/votd").let {
                assertNotNull(it)
                assertEquals(VerseOfTheDay, it.originalRoute)
            }
            navGraph.findMatch("/app/verses").let {
                assertNotNull(it)
                assertEquals(List, it.originalRoute)
            }
            navGraph.findMatch("/app/verses/123").let {
                assertNotNull(it)
                assertEquals(Detail, it.originalRoute)
            }
            navGraph.findMatch("/app/verses/new").let {
                assertNotNull(it)
                assertEquals(Create, it.originalRoute)
            }
            navGraph.findMatch("/app/verses/123/edit").let {
                assertNotNull(it)
                assertEquals(EditVerse, it.originalRoute)
            }
        }
    }
}
