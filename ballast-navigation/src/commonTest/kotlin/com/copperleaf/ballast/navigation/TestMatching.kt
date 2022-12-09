package com.copperleaf.ballast.navigation

import com.copperleaf.ballast.navigation.routing.Destination
import com.copperleaf.ballast.navigation.routing.UnmatchedDestination
import com.copperleaf.ballast.navigation.routing.matchDestination
import com.copperleaf.ballast.navigation.routing.matchDestinationOrThrow
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertSame
import kotlin.test.assertTrue

class TestMatching {

    fun String.shouldMatch(
        route: SimpleRoute,
        expectedPathParameters: Map<String, List<String>> = emptyMap(),
        expectedQueryParameters: Map<String, List<String>> = emptyMap(),
    ) {
        val match = route.matcher.matchDestinationOrThrow(route, UnmatchedDestination.parse(this))
        assertEquals(expectedPathParameters, match.pathParameters)
        assertEquals(expectedQueryParameters, match.queryParameters)
    }

    fun String.shouldNotMatch(
        route: SimpleRoute,
        expectedErrorMessage: String,
    ) {
        assertFailsWith<IllegalStateException> {
            route.matcher.matchDestinationOrThrow(route, UnmatchedDestination.parse(this))
        }.also {
            assertEquals(expectedErrorMessage, it.message)
        }
    }

    @Test
    fun testMatchPath() {
        SimpleRoute("/one").apply {
            "/one".shouldMatch(this)
            "/".shouldNotMatch(
                this,
                expectedErrorMessage = "Destination '/' does not match Route '/one': Path mismatch"
            )
            "/two".shouldNotMatch(
                this,
                expectedErrorMessage = "Destination '/two' does not match Route '/one': Path mismatch"
            )
            "/one/two".shouldNotMatch(
                this,
                expectedErrorMessage = "Destination '/one/two' does not match Route '/one': Path mismatch"
            )
        }
        SimpleRoute("/*").apply {
            "/one".shouldMatch(this)
            "/two".shouldMatch(this)
            "/".shouldNotMatch(
                this,
                expectedErrorMessage = "Destination '/' does not match Route '/*': Path mismatch"
            )
            "/one/two".shouldNotMatch(
                this,
                expectedErrorMessage = "Destination '/one/two' does not match Route '/*': Path mismatch"
            )
        }
        SimpleRoute("/:one").apply {
            "/two".shouldMatch(
                this,
                expectedPathParameters = mapOf("one" to listOf("two")),
            )
            "/three".shouldMatch(
                this,
                expectedPathParameters = mapOf("one" to listOf("three")),
            )
            "/".shouldNotMatch(
                this,
                expectedErrorMessage = "Destination '/' does not match Route '/:one': Path mismatch"
            )
            "/one/two".shouldNotMatch(
                this,
                expectedErrorMessage = "Destination '/one/two' does not match Route '/:one': Path mismatch"
            )
        }
        SimpleRoute("/{one}").apply {
            "/two".shouldMatch(
                this,
                expectedPathParameters = mapOf("one" to listOf("two")),
            )
            "/three".shouldMatch(
                this,
                expectedPathParameters = mapOf("one" to listOf("three")),
            )
            "/".shouldNotMatch(
                this,
                expectedErrorMessage = "Destination '/' does not match Route '/{one}': Path mismatch"
            )
            "/one/two".shouldNotMatch(
                this,
                expectedErrorMessage = "Destination '/one/two' does not match Route '/{one}': Path mismatch"
            )
        }
        SimpleRoute("/{one?}").apply {
            "/two".shouldMatch(
                this,
                expectedPathParameters = mapOf("one" to listOf("two")),
            )
            "/three".shouldMatch(
                this,
                expectedPathParameters = mapOf("one" to listOf("three")),
            )
            "/".shouldMatch(
                this,
                expectedPathParameters = emptyMap(),
            )
            "/one/two".shouldNotMatch(
                this,
                expectedErrorMessage = "Destination '/one/two' does not match Route '/{one?}': Path mismatch",
            )
        }
        SimpleRoute("/{...}").apply {
            "/two".shouldMatch(
                this,
                expectedPathParameters = emptyMap(),
            )
            "/three".shouldMatch(
                this,
                expectedPathParameters = emptyMap(),
            )
            "/".shouldMatch(
                this,
                expectedPathParameters = emptyMap(),
            )
            "/one/two".shouldMatch(
                this,
                expectedPathParameters = emptyMap(),
            )
        }
        SimpleRoute("/{one...}").apply {
            "/two".shouldMatch(
                this,
                expectedPathParameters = mapOf("one" to listOf("two")),
            )
            "/three".shouldMatch(
                this,
                expectedPathParameters = mapOf("one" to listOf("three")),
            )
            "/".shouldMatch(
                this,
                expectedPathParameters = mapOf("one" to emptyList()),
            )
            "/one/two".shouldMatch(
                this,
                expectedPathParameters = mapOf("one" to listOf("one", "two")),
            )
        }

        SimpleRoute("/one/:two/three/{four}/*/{five...}").apply {
            "/one/TWO/three/FOUR/FIVE".shouldMatch(
                this,
                expectedPathParameters = mapOf(
                    "two" to listOf("TWO"),
                    "four" to listOf("FOUR"),
                    "five" to emptyList(),
                ),
            )
            "/one/TWO/three/FOUR/FIVE/six/seven/eight".shouldMatch(
                this,
                expectedPathParameters = mapOf(
                    "two" to listOf("TWO"),
                    "four" to listOf("FOUR"),
                    "five" to listOf("six", "seven", "eight"),
                ),
            )
        }
    }

    @Test
    fun testMatchQuery() {
        SimpleRoute("/one?one=two").apply {
            "/one?one=two".shouldMatch(
                this,
                expectedQueryParameters = mapOf(),
            )
            "/one".shouldNotMatch(
                this,
                expectedErrorMessage = "Destination '/one' does not match Route '/one?one=two': Query string mismatch"
            )
            "/one?".shouldNotMatch(
                this,
                expectedErrorMessage = "Destination '/one?' does not match Route '/one?one=two': Query string mismatch"
            )
            "/one?one=three".shouldNotMatch(
                this,
                expectedErrorMessage = "Destination '/one?one=three' does not match Route '/one?one=two': Query string mismatch"
            )
            "/one?two=two".shouldNotMatch(
                this,
                expectedErrorMessage = "Destination '/one?two=two' does not match Route '/one?one=two': Query string mismatch"
            )
            "/one?one=two&three=four".shouldNotMatch(
                this,
                expectedErrorMessage = "Destination '/one?one=two&three=four' does not match Route '/one?one=two': Query string mismatch"
            )
            "/one?one=two&one=three".shouldNotMatch(
                this,
                expectedErrorMessage = "Destination '/one?one=two&one=three' does not match Route '/one?one=two': Query string mismatch"
            )
        }
        SimpleRoute("/one?one={!}").apply {
            "/one?one=two".shouldMatch(
                this,
                expectedQueryParameters = mapOf("one" to listOf("two")),
            )
            "/one".shouldNotMatch(
                this,
                expectedErrorMessage = "Destination '/one' does not match Route '/one?one={!}': Query string mismatch"
            )
            "/one?".shouldNotMatch(
                this,
                expectedErrorMessage = "Destination '/one?' does not match Route '/one?one={!}': Query string mismatch"
            )
            "/one?one=three".shouldMatch(
                this,
                expectedQueryParameters = mapOf("one" to listOf("three")),
            )
            "/one?two=two".shouldNotMatch(
                this,
                expectedErrorMessage = "Destination '/one?two=two' does not match Route '/one?one={!}': Query string mismatch"
            )
            "/one?one=two&three=four".shouldNotMatch(
                this,
                expectedErrorMessage = "Destination '/one?one=two&three=four' does not match Route '/one?one={!}': Query string mismatch"
            )
            "/one?one=two&one=three".shouldNotMatch(
                this,
                expectedErrorMessage = "Destination '/one?one=two&one=three' does not match Route '/one?one={!}': Query string mismatch"
            )
        }
        SimpleRoute("/one?one={[!]}").apply {
            "/one?one=two".shouldMatch(
                this,
                expectedQueryParameters = mapOf("one" to listOf("two")),
            )
            "/one".shouldNotMatch(
                this,
                expectedErrorMessage = "Destination '/one' does not match Route '/one?one={[!]}': Query string mismatch"
            )
            "/one?".shouldNotMatch(
                this,
                expectedErrorMessage = "Destination '/one?' does not match Route '/one?one={[!]}': Query string mismatch"
            )
            "/one?one=three".shouldMatch(
                this,
                expectedQueryParameters = mapOf("one" to listOf("three")),
            )
            "/one?two=two".shouldNotMatch(
                this,
                expectedErrorMessage = "Destination '/one?two=two' does not match Route '/one?one={[!]}': Query string mismatch"
            )
            "/one?one=two&three=four".shouldNotMatch(
                this,
                expectedErrorMessage = "Destination '/one?one=two&three=four' does not match Route '/one?one={[!]}': Query string mismatch"
            )
            "/one?one=two&one=three".shouldMatch(
                this,
                expectedQueryParameters = mapOf("one" to listOf("two", "three")),
            )
        }
        SimpleRoute("/one?one={?}").apply {
            "/one?one=two".shouldMatch(
                this,
                expectedQueryParameters = mapOf("one" to listOf("two")),
            )
            "/one".shouldMatch(
                this,
                expectedQueryParameters = mapOf(),
            )
            "/one?".shouldMatch(
                this,
                expectedQueryParameters = mapOf(),
            )
            "/one?one=three".shouldMatch(
                this,
                expectedQueryParameters = mapOf("one" to listOf("three")),
            )
            "/one?two=two".shouldNotMatch(
                this,
                expectedErrorMessage = "Destination '/one?two=two' does not match Route '/one?one={?}': Query string mismatch"
            )
            "/one?one=two&three=four".shouldNotMatch(
                this,
                expectedErrorMessage = "Destination '/one?one=two&three=four' does not match Route '/one?one={?}': Query string mismatch"
            )
            "/one?one=two&one=three".shouldNotMatch(
                this,
                expectedErrorMessage = "Destination '/one?one=two&one=three' does not match Route '/one?one={?}': Query string mismatch"
            )
        }
        SimpleRoute("/one?one={[?]}").apply {
            "/one?one=two".shouldMatch(
                this,
                expectedQueryParameters = mapOf("one" to listOf("two")),
            )
            "/one".shouldMatch(
                this,
                expectedQueryParameters = mapOf(),
            )
            "/one?".shouldMatch(
                this,
                expectedQueryParameters = mapOf(),
            )
            "/one?one=three".shouldMatch(
                this,
                expectedQueryParameters = mapOf("one" to listOf("three")),
            )
            "/one?two=two".shouldNotMatch(
                this,
                expectedErrorMessage = "Destination '/one?two=two' does not match Route '/one?one={[?]}': Query string mismatch"
            )
            "/one?one=two&three=four".shouldNotMatch(
                this,
                expectedErrorMessage = "Destination '/one?one=two&three=four' does not match Route '/one?one={[?]}': Query string mismatch"
            )
            "/one?one=two&one=three".shouldMatch(
                this,
                expectedQueryParameters = mapOf("one" to listOf("two", "three")),
            )
        }
        SimpleRoute("/one?{...}").apply {
            "/one?one=two".shouldMatch(
                this,
                expectedQueryParameters = mapOf("one" to listOf("two")),
            )
            "/one".shouldMatch(
                this,
                expectedQueryParameters = mapOf(),
            )
            "/one?".shouldMatch(
                this,
                expectedQueryParameters = mapOf(),
            )
            "/one?one=three".shouldMatch(
                this,
                expectedQueryParameters = mapOf("one" to listOf("three")),
            )
            "/one?two=two".shouldMatch(
                this,
                expectedQueryParameters = mapOf("two" to listOf("two")),
            )
            "/one?one=two&three=four".shouldMatch(
                this,
                expectedQueryParameters = mapOf("one" to listOf("two"), "three" to listOf("four")),
            )
            "/one?one=two&one=three".shouldMatch(
                this,
                expectedQueryParameters = mapOf("one" to listOf("two", "three")),
            )
        }
    }

    @Test
    fun testRoutePriority() {
        val pathRoute = SimpleRoute("/one/{two?}?three={!}")
        val queryRoute = SimpleRoute("/one?two={?}&three={!}")
        val simpleRoutingTable = SimpleRoutingTable(queryRoute, pathRoute)
        val unmatchedDestination = UnmatchedDestination.parse("/one?three=four")

        // check that the path route should be matched over the query route
        assertTrue { pathRoute.matcher.weight > queryRoute.matcher.weight }

        // check that both routes can be matched to the same destination
        assertTrue { pathRoute.matcher.matchDestination(pathRoute, unmatchedDestination) is Destination.Match }
        assertTrue { queryRoute.matcher.matchDestination(queryRoute, unmatchedDestination) is Destination.Match }

        // check that the RoutingTable selects the right route, based on weight
        val destination = simpleRoutingTable.findMatch(unmatchedDestination)
        assertTrue { destination is Destination.Match }
        assertSame(pathRoute, (destination as Destination.Match).originalRoute)
    }

    @Test
    fun testRoutePriorityWithHardcodedWeights() {
        val pathRoute = SimpleRoute("/one/{two?}?three={!}")
        val queryRoute = SimpleRoute("/one?two={?}&three={!}", Double.MAX_VALUE)
        val simpleRoutingTable = SimpleRoutingTable(queryRoute, pathRoute)
        val unmatchedDestination = UnmatchedDestination.parse("/one?three=four")

        // check that the path route should be matched over the query route
        assertTrue { pathRoute.matcher.weight < queryRoute.matcher.weight }

        // check that both routes can be matched to the same destination
        assertTrue { pathRoute.matcher.matchDestination(pathRoute, unmatchedDestination) is Destination.Match }
        assertTrue { queryRoute.matcher.matchDestination(queryRoute, unmatchedDestination) is Destination.Match }

        // check that the RoutingTable selects the right route, based on weight
        val destination = simpleRoutingTable.findMatch(unmatchedDestination)
        assertTrue { destination is Destination.Match }
        assertSame(queryRoute, (destination as Destination.Match).originalRoute)
    }


    @Test
    fun testRoutePriorityWithManyQueryParameters() {
        val pathRoute = SimpleRoute("/one/{two?}?three={!}")
        val queryRoute = SimpleRoute("/one?two={?}&three={!}&four={?}")
        val simpleRoutingTable = SimpleRoutingTable(queryRoute, pathRoute)
        val unmatchedDestination = UnmatchedDestination.parse("/one?three=four")

        // check that the path route should be matched over the query route
        assertTrue { pathRoute.matcher.weight < queryRoute.matcher.weight }

        // check that both routes can be matched to the same destination
        assertTrue { pathRoute.matcher.matchDestination(pathRoute, unmatchedDestination) is Destination.Match }
        assertTrue { queryRoute.matcher.matchDestination(queryRoute, unmatchedDestination) is Destination.Match }

        // check that the RoutingTable selects the right route, based on weight
        val destination = simpleRoutingTable.findMatch(unmatchedDestination)
        assertTrue { destination is Destination.Match }
        assertSame(queryRoute, (destination as Destination.Match).originalRoute)
    }
}
