package com.copperleaf.ballast.navigation

import com.copperleaf.ballast.navigation.internal.PathParser
import com.copperleaf.ballast.navigation.internal.QueryStringParser
import com.copperleaf.ballast.navigation.routing.PathSegment
import com.copperleaf.ballast.navigation.routing.QueryParameter
import com.copperleaf.ballast.navigation.routing.RouteMatcher
import io.kotest.core.spec.style.StringSpec

class TestRouteParser : StringSpec({
// Path
// ---------------------------------------------------------------------------------------------------------------------

    "testPathSegmentParser" {
        PathParser.parsePathSegment("one").apply {
            Assertions.assertEquals(PathSegment.Static("one"), this)
            Assertions.assertEquals(5, weight)
        }
        PathParser.parsePathSegment("two").apply {
            Assertions.assertEquals(PathSegment.Static("two"), this)
            Assertions.assertEquals(5, weight)
        }
        PathParser.parsePathSegment(":one").apply {
            Assertions.assertEquals(PathSegment.Parameter("one", false), this)
            Assertions.assertEquals(4, weight)
        }
        PathParser.parsePathSegment("{one}").apply {
            Assertions.assertEquals(PathSegment.Parameter("one", false), this)
            Assertions.assertEquals(4, weight)
        }
        PathParser.parsePathSegment("*").apply {
            Assertions.assertEquals(PathSegment.Wildcard, this)
            Assertions.assertEquals(3, weight)
        }
        PathParser.parsePathSegment("{one?}").apply {
            Assertions.assertEquals(PathSegment.Parameter("one", true), this)
            Assertions.assertEquals(2, weight)
        }
        PathParser.parsePathSegment("{...}").apply {
            Assertions.assertEquals(PathSegment.Tailcard(null), this)
            Assertions.assertEquals(1, weight)
        }
        PathParser.parsePathSegment("{one...}").apply {
            Assertions.assertEquals(PathSegment.Tailcard("one"), this)
            Assertions.assertEquals(1, weight)
        }
    }

    "testPathParser" {
        PathParser.parsePath("/").apply {
            Assertions.assertEquals(listOf<PathSegment>(), this)
        }
        PathParser.parsePath("/one").apply {
            Assertions.assertEquals(listOf(PathSegment.Static("one")), this)
        }
        PathParser.parsePath("/two").apply {
            Assertions.assertEquals(listOf(PathSegment.Static("two")), this)
        }
        PathParser.parsePath("/one-two").apply {
            Assertions.assertEquals(listOf(PathSegment.Static("one-two")), this)
        }
        PathParser.parsePath("/one_two").apply {
            Assertions.assertEquals(listOf(PathSegment.Static("one_two")), this)
        }
        PathParser.parsePath("/:one").apply {
            Assertions.assertEquals(listOf(PathSegment.Parameter("one", false)), this)
        }
        PathParser.parsePath("/{one}").apply {
            Assertions.assertEquals(listOf(PathSegment.Parameter("one", false)), this)
        }
        PathParser.parsePath("/*").apply {
            Assertions.assertEquals(listOf(PathSegment.Wildcard), this)
        }
        PathParser.parsePath("/{one?}").apply {
            Assertions.assertEquals(listOf(PathSegment.Parameter("one", true)), this)
        }
        PathParser.parsePath("/{...}").apply {
            Assertions.assertEquals(listOf(PathSegment.Tailcard(null)), this)
        }
        PathParser.parsePath("/{one...}").apply {
            Assertions.assertEquals(listOf(PathSegment.Tailcard("one")), this)
        }

        PathParser.parsePath("/one/two/*/:three/{four}/{five?}").apply {
            Assertions.assertEquals(
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
            Assertions.assertEquals(
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
            Assertions.assertEquals(
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
            Assertions.assertEquals(
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

    "testQueryParameterParser" {
        QueryStringParser.parseQueryParameter("one=two").apply {
            Assertions.assertEquals(QueryParameter.Static("one", listOf("two")), this)
            Assertions.assertEquals(5, weight)
        }
        QueryStringParser.parseQueryParameter("one=[two,three,four]").apply {
            Assertions.assertEquals(QueryParameter.Static("one", listOf("four", "three", "two")), this)
            Assertions.assertEquals(5, weight)
        }
        QueryStringParser.parseQueryParameter("one={!}").apply {
            Assertions.assertEquals(QueryParameter.Parameter("one", false, false), this)
            Assertions.assertEquals(4, weight)
        }
        QueryStringParser.parseQueryParameter("one={[!]}").apply {
            Assertions.assertEquals(QueryParameter.Parameter("one", false, true), this)
            Assertions.assertEquals(4, weight)
        }
        QueryStringParser.parseQueryParameter("one={?}").apply {
            Assertions.assertEquals(QueryParameter.Parameter("one", true, false), this)
            Assertions.assertEquals(2, weight)
        }
        QueryStringParser.parseQueryParameter("one={[?]}").apply {
            Assertions.assertEquals(QueryParameter.Parameter("one", true, true), this)
            Assertions.assertEquals(2, weight)
        }
        QueryStringParser.parseQueryParameter("{...}").apply {
            Assertions.assertEquals(QueryParameter.Remainder, this)
            Assertions.assertEquals(1, weight)
        }
    }

    "testQueryStringParser" {
        QueryStringParser.parseQueryString("one=two").apply {
            Assertions.assertEquals(listOf(QueryParameter.Static("one", listOf("two"))), this)
        }
        QueryStringParser.parseQueryString("one=[two,three,four]").apply {
            Assertions.assertEquals(listOf(QueryParameter.Static("one", listOf("four", "three", "two"))), this)
        }
        QueryStringParser.parseQueryString("one={!}").apply {
            Assertions.assertEquals(listOf(QueryParameter.Parameter("one", false, false)), this)
        }
        QueryStringParser.parseQueryString("one={[!]}").apply {
            Assertions.assertEquals(listOf(QueryParameter.Parameter("one", false, true)), this)
        }
        QueryStringParser.parseQueryString("one={?}").apply {
            Assertions.assertEquals(listOf(QueryParameter.Parameter("one", true, false)), this)
        }
        QueryStringParser.parseQueryString("one={[?]}").apply {
            Assertions.assertEquals(listOf(QueryParameter.Parameter("one", true, true)), this)
        }
        QueryStringParser.parseQueryString("{...}").apply {
            Assertions.assertEquals(listOf(QueryParameter.Remainder), this)
        }

        QueryStringParser.parseQueryString("one=two&three=[four,five]&six={!}&seven={[?]}&{...}").apply {
            Assertions.assertEquals(
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

    "testCreateMatcher" {
        fun String.createMatcher() = RouteMatcher.create(this)

        "/one".createMatcher().apply {
            Assertions.assertEquals(
                listOf(
                    PathSegment.Static("one"),
                ),
                path
            )
            Assertions.assertEquals(50.0, weight)
        }
        "/one/two".createMatcher().apply {
            Assertions.assertEquals(
                listOf(
                    PathSegment.Static("one"),
                    PathSegment.Static("two"),
                ),
                path
            )
            Assertions.assertEquals(550.0, weight)
        }
        "/one/:two".createMatcher().apply {
            Assertions.assertEquals(
                listOf(
                    PathSegment.Static("one"),
                    PathSegment.Parameter("two", false),
                ),
                path
            )
            Assertions.assertEquals(540.0, weight)
        }
        "/one/:two/{three?}".createMatcher().apply {
            Assertions.assertEquals(
                listOf(
                    PathSegment.Static("one"),
                    PathSegment.Parameter("two", false),
                    PathSegment.Parameter("three", true),
                ),
                path
            )
            Assertions.assertEquals(5420.0, weight)
        }
        "/one/:two/{three}/*/{four}".createMatcher().apply {
            Assertions.assertEquals(
                listOf(
                    PathSegment.Static("one"),
                    PathSegment.Parameter("two", false),
                    PathSegment.Parameter("three", false),
                    PathSegment.Wildcard,
                    PathSegment.Parameter("four", false),
                ),
                path
            )
            Assertions.assertEquals(544340.0, weight)
        }

        "/one/:two/{three?}?one=two".createMatcher().apply {
            Assertions.assertEquals(
                listOf(
                    PathSegment.Static("one"),
                    PathSegment.Parameter("two", false),
                    PathSegment.Parameter("three", true),
                ),
                path
            )
            Assertions.assertEquals(
                listOf(
                    QueryParameter.Static("one", listOf("two")),
                ),
                query
            )
            Assertions.assertEquals(5420_5.0, weight)
        }
        "/one/:two/{three}/{...}?one=two&four={!}&{...}".createMatcher().apply {
            Assertions.assertEquals(
                listOf(
                    PathSegment.Static("one"),
                    PathSegment.Parameter("two", false),
                    PathSegment.Parameter("three", false),
                    PathSegment.Tailcard(null),
                ),
                path
            )
            Assertions.assertEquals(
                listOf(
                    QueryParameter.Static("one", listOf("two")),
                    QueryParameter.Parameter("four", false, false),
                    QueryParameter.Remainder,
                ),
                query
            )
            Assertions.assertEquals(54410_541.0, weight)
        }

        Assertions.assertFailsWith<Throwable> { "/one/:two/{three?}/{four?}".createMatcher() }.let {
            Assertions.assertEquals("you can only have one optional parameter or tailcard, but not both", it.message)
        }
        Assertions.assertFailsWith<Throwable> { "/one/:two/{three?}/{...}".createMatcher() }.let {
            Assertions.assertEquals("you can only have one optional parameter or tailcard, but not both", it.message)
        }
        Assertions.assertFailsWith<Throwable> { "/one/:two/{three?}/{four...}".createMatcher() }.let {
            Assertions.assertEquals("you can only have one optional parameter or tailcard, but not both", it.message)
        }
        Assertions.assertFailsWith<Throwable> { "/one/:two/{three?}/four".createMatcher() }.let {
            Assertions.assertEquals("optional parameters and tailcards must be at the end of the path", it.message)
        }
        Assertions.assertFailsWith<Throwable> { "/one/:two/{two}".createMatcher() }.let {
            Assertions.assertEquals("parameter names must be unique", it.message)
        }
        Assertions.assertFailsWith<Throwable> { "/one/:two/{two...}".createMatcher() }.let {
            Assertions.assertEquals("parameter names must be unique", it.message)
        }
        Assertions.assertFailsWith<Throwable> { "one/two".createMatcher() }.let {
            Assertions.assertEquals(
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

//    "testPrependPath" {
//        URLBuilder()
//            .apply { encodedPath = "/one/two/three" }.build()
//            .prependPath("arkham-explorer")
//            .let { assertEquals("/arkham-explorer/one/two/three", it.toString()) }
//    }
//
//    fun Url.prependPath(basePath: String?): Url {
//        return if (basePath != null) {
//            URLBuilder(this)
//                .also {
//                    it.encodedPath = "${basePath}/${encodedPath}"
//                }
//                .build()
//        } else {
//            this
//        }
//    }
})
