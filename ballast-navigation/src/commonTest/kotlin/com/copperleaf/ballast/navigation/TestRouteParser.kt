package com.copperleaf.ballast.navigation

import com.copperleaf.ballast.navigation.internal.PathParser
import com.copperleaf.ballast.navigation.internal.QueryStringParser
import com.copperleaf.ballast.navigation.routing.PathSegment
import com.copperleaf.ballast.navigation.routing.QueryParameter
import com.copperleaf.ballast.navigation.routing.RouteMatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class TestRouteParser {
// Path
// ---------------------------------------------------------------------------------------------------------------------

    @Test
    fun testPathSegmentParser() = runTest {
        PathParser.parsePathSegment("one").apply {
            assertEquals(PathSegment.Static("one"), this)
            assertEquals(5, weight)
        }
        PathParser.parsePathSegment("two").apply {
            assertEquals(PathSegment.Static("two"), this)
            assertEquals(5, weight)
        }
        PathParser.parsePathSegment(":one").apply {
            assertEquals(PathSegment.Parameter("one", false), this)
            assertEquals(4, weight)
        }
        PathParser.parsePathSegment("{one}").apply {
            assertEquals(PathSegment.Parameter("one", false), this)
            assertEquals(4, weight)
        }
        PathParser.parsePathSegment("*").apply {
            assertEquals(PathSegment.Wildcard, this)
            assertEquals(3, weight)
        }
        PathParser.parsePathSegment("{one?}").apply {
            assertEquals(PathSegment.Parameter("one", true), this)
            assertEquals(2, weight)
        }
        PathParser.parsePathSegment("{...}").apply {
            assertEquals(PathSegment.Tailcard(null), this)
            assertEquals(1, weight)
        }
        PathParser.parsePathSegment("{one...}").apply {
            assertEquals(PathSegment.Tailcard("one"), this)
            assertEquals(1, weight)
        }
    }

    @Test
    fun testPathParser() = runTest {
        PathParser.parsePath("/").apply {
            assertEquals(listOf<PathSegment>(), this)
        }
        PathParser.parsePath("/one").apply {
            assertEquals(listOf(PathSegment.Static("one")), this)
        }
        PathParser.parsePath("/two").apply {
            assertEquals(listOf(PathSegment.Static("two")), this)
        }
        PathParser.parsePath("/one-two").apply {
            assertEquals(listOf(PathSegment.Static("one-two")), this)
        }
        PathParser.parsePath("/one_two").apply {
            assertEquals(listOf(PathSegment.Static("one_two")), this)
        }
        PathParser.parsePath("/:one").apply {
            assertEquals(listOf(PathSegment.Parameter("one", false)), this)
        }
        PathParser.parsePath("/{one}").apply {
            assertEquals(listOf(PathSegment.Parameter("one", false)), this)
        }
        PathParser.parsePath("/*").apply {
            assertEquals(listOf(PathSegment.Wildcard), this)
        }
        PathParser.parsePath("/{one?}").apply {
            assertEquals(listOf(PathSegment.Parameter("one", true)), this)
        }
        PathParser.parsePath("/{...}").apply {
            assertEquals(listOf(PathSegment.Tailcard(null)), this)
        }
        PathParser.parsePath("/{one...}").apply {
            assertEquals(listOf(PathSegment.Tailcard("one")), this)
        }

        PathParser.parsePath("/one/two/*/:three/{four}/{five?}").apply {
            assertEquals(
                listOf(
                    PathSegment.Static("one"),
                    PathSegment.Static("two"),
                    PathSegment.Wildcard,
                    PathSegment.Parameter("three", false),
                    PathSegment.Parameter("four", false),
                    PathSegment.Parameter("five", true),
                ), this
            )
        }
        PathParser.parsePath("/one/two/*/:three/{four}/{...}").apply {
            assertEquals(
                listOf(
                    PathSegment.Static("one"),
                    PathSegment.Static("two"),
                    PathSegment.Wildcard,
                    PathSegment.Parameter("three", false),
                    PathSegment.Parameter("four", false),
                    PathSegment.Tailcard(null),
                ), this
            )
        }
        PathParser.parsePath("/one/two/*/:three/{four}/{five...}").apply {
            assertEquals(
                listOf(
                    PathSegment.Static("one"),
                    PathSegment.Static("two"),
                    PathSegment.Wildcard,
                    PathSegment.Parameter("three", false),
                    PathSegment.Parameter("four", false),
                    PathSegment.Tailcard("five"),
                ), this
            )
        }

        // the parser does not validate whether the segments are correct, it's just checking the format
        PathParser.parsePath("/one/two/*/:three/{four}/{five...}/{six?}/{seven...}/*/{...}").apply {
            assertEquals(
                listOf(
                    PathSegment.Static("one"),
                    PathSegment.Static("two"),
                    PathSegment.Wildcard,
                    PathSegment.Parameter("three", false),
                    PathSegment.Parameter("four", false),
                    PathSegment.Tailcard("five"),
                    PathSegment.Parameter("six", true),
                    PathSegment.Tailcard("seven"),
                    PathSegment.Wildcard,
                    PathSegment.Tailcard(null),
                ), this
            )
        }
    }

// Query Parameters
// ---------------------------------------------------------------------------------------------------------------------

    @Test
    fun testQueryParameterParser() = runTest {
        QueryStringParser.parseQueryParameter("one=two").apply {
            assertEquals(QueryParameter.Static("one", listOf("two")), this)
            assertEquals(5, weight)
        }
        QueryStringParser.parseQueryParameter("one=[two,three,four]").apply {
            assertEquals(QueryParameter.Static("one", listOf("four", "three", "two")), this)
            assertEquals(5, weight)
        }
        QueryStringParser.parseQueryParameter("one={!}").apply {
            assertEquals(QueryParameter.Parameter("one", false, false), this)
            assertEquals(4, weight)
        }
        QueryStringParser.parseQueryParameter("one={[!]}").apply {
            assertEquals(QueryParameter.Parameter("one", false, true), this)
            assertEquals(4, weight)
        }
        QueryStringParser.parseQueryParameter("one={?}").apply {
            assertEquals(QueryParameter.Parameter("one", true, false), this)
            assertEquals(2, weight)
        }
        QueryStringParser.parseQueryParameter("one={[?]}").apply {
            assertEquals(QueryParameter.Parameter("one", true, true), this)
            assertEquals(2, weight)
        }
        QueryStringParser.parseQueryParameter("{...}").apply {
            assertEquals(QueryParameter.Remainder, this)
            assertEquals(1, weight)
        }
    }

    @Test
    fun testQueryStringParser() = runTest {
        QueryStringParser.parseQueryString("one=two").apply {
            assertEquals(listOf(QueryParameter.Static("one", listOf("two"))), this)
        }
        QueryStringParser.parseQueryString("one=[two,three,four]").apply {
            assertEquals(listOf(QueryParameter.Static("one", listOf("four", "three", "two"))), this)
        }
        QueryStringParser.parseQueryString("one={!}").apply {
            assertEquals(listOf(QueryParameter.Parameter("one", false, false)), this)
        }
        QueryStringParser.parseQueryString("one={[!]}").apply {
            assertEquals(listOf(QueryParameter.Parameter("one", false, true)), this)
        }
        QueryStringParser.parseQueryString("one={?}").apply {
            assertEquals(listOf(QueryParameter.Parameter("one", true, false)), this)
        }
        QueryStringParser.parseQueryString("one={[?]}").apply {
            assertEquals(listOf(QueryParameter.Parameter("one", true, true)), this)
        }
        QueryStringParser.parseQueryString("{...}").apply {
            assertEquals(listOf(QueryParameter.Remainder), this)
        }

        QueryStringParser.parseQueryString("one=two&three=[four,five]&six={!}&seven={[?]}&{...}").apply {
            assertEquals(
                listOf(
                    QueryParameter.Static("one", listOf("two")),
                    QueryParameter.Static("three", listOf("five", "four")),
                    QueryParameter.Parameter("six", false, false),
                    QueryParameter.Parameter("seven", true, true),
                    QueryParameter.Remainder,
                ), this
            )
        }
    }

    @Test
    fun testCreateMatcher() = runTest {
        fun String.createMatcher() = RouteMatcher.create(this)

        "/one".createMatcher().apply {
            assertEquals(
                listOf(
                    PathSegment.Static("one"),
                ),
                path
            )
            assertEquals(50.0, weight)
        }
        "/one/two".createMatcher().apply {
            assertEquals(
                listOf(
                    PathSegment.Static("one"),
                    PathSegment.Static("two"),
                ),
                path
            )
            assertEquals(550.0, weight)
        }
        "/one/:two".createMatcher().apply {
            assertEquals(
                listOf(
                    PathSegment.Static("one"),
                    PathSegment.Parameter("two", false),
                ),
                path
            )
            assertEquals(540.0, weight)
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
            assertEquals(5420.0, weight)
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
            assertEquals(544340.0, weight)
        }

        "/one/:two/{three?}?one=two".createMatcher().apply {
            assertEquals(
                listOf(
                    PathSegment.Static("one"),
                    PathSegment.Parameter("two", false),
                    PathSegment.Parameter("three", true),
                ),
                path
            )
            assertEquals(
                listOf(
                    QueryParameter.Static("one", listOf("two")),
                ),
                query
            )
            assertEquals(5420_5.0, weight)
        }
        "/one/:two/{three}/{...}?one=two&four={!}&{...}".createMatcher().apply {
            assertEquals(
                listOf(
                    PathSegment.Static("one"),
                    PathSegment.Parameter("two", false),
                    PathSegment.Parameter("three", false),
                    PathSegment.Tailcard(null),
                ),
                path
            )
            assertEquals(
                listOf(
                    QueryParameter.Static("one", listOf("two")),
                    QueryParameter.Parameter("four", false, false),
                    QueryParameter.Remainder,
                ),
                query
            )
            assertEquals(54410_541.0, weight)
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
        assertFailsWith<Throwable> { "one/two".createMatcher() }.let {
            assertEquals(
                """
                |Parse error at 1:1 (LeadingSlashParser)
                |
                |Path must start with a leading slash
                |
                |1|one/two
                |>>^
                """.trimMargin().trim(),
                it.message
            )
        }
    }
}
