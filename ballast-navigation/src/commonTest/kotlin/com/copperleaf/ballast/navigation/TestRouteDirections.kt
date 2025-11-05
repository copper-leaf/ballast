package com.copperleaf.ballast.navigation

import com.copperleaf.ballast.navigation.routing.Destination
import com.copperleaf.ballast.navigation.routing.Route
import com.copperleaf.ballast.navigation.routing.UnmatchedDestination
import com.copperleaf.ballast.navigation.routing.build
import com.copperleaf.ballast.navigation.routing.directions
import com.copperleaf.ballast.navigation.routing.matchDestination
import com.copperleaf.ballast.navigation.routing.path
import com.copperleaf.ballast.navigation.routing.pathParameter
import com.copperleaf.ballast.navigation.routing.queryParameter
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue

class TestRouteDirections {
// Path Tests - Success
// ---------------------------------------------------------------------------------------------------------------------

    @Test
    fun testNoPathParametersAllowed() = runTest {
        SimpleRoute("/").apply {
            directions()
                .shouldBe("/")
        }
        SimpleRoute("/one").apply {
            directions()
                .shouldBe("/one")
        }
    }

    @Test
    fun test1RequiredPathParameter() = runTest {
        SimpleRoute("/one/:two").apply {
            directions()
                .pathParameter("two", "three")
                .shouldBe("/one/three")
            directions()
                .pathParameter("two", listOf("three"))
                .shouldBe("/one/three")
            directions()
                .path("three")
                .shouldBe("/one/three")
        }
    }

    @Test
    fun test1OptionalPathParameter() = runTest {
        SimpleRoute("/one/{two?}").apply {
            directions()
                .shouldBe("/one")
            directions()
                .pathParameter("two", "three")
                .shouldBe("/one/three")
            directions()
                .pathParameter("two", listOf("three"))
                .shouldBe("/one/three")
            directions()
                .path("three")
                .shouldBe("/one/three")
        }
    }

    @Test
    fun testMultiplePathParameters() = runTest {
        SimpleRoute("/one/:two/{three?}").apply {
            directions()
                .pathParameter("two", "three")
                .shouldBe("/one/three")
            directions()
                .path("three")
                .shouldBe("/one/three")

            directions()
                .pathParameter("two", "three")
                .pathParameter("three", "four")
                .shouldBe("/one/three/four")
            directions()
                .path("three", "four")
                .shouldBe("/one/three/four")
        }
    }

    @Test
    fun testPathAnonymousTailcard() = runTest {
        SimpleRoute("/one/{...}").apply {
            directions()
                .shouldBe("/one")
        }
    }

    @Test
    fun testPathNamedTailcard() = runTest {
        SimpleRoute("/one/{two...}").apply {
            directions()
                .pathParameter("two", "three")
                .shouldBe("/one/three")
            directions()
                .pathParameter("two", "three", "four")
                .shouldBe("/one/three/four")
            directions()
                .pathParameter("two", listOf("three"))
                .shouldBe("/one/three")
            directions()
                .pathParameter("two", listOf("three", "four"))
                .shouldBe("/one/three/four")
            directions()
                .path("two", "three", "four")
                .shouldBe("/one/two/three/four")
        }
    }

    @Test
    fun testPathParameterWithAnonymousTailcard() = runTest {
        SimpleRoute("/one/{two}/{...}").apply {
            directions()
                .pathParameter("two", "three")
                .shouldBe("/one/three")
        }
    }

    @Test
    fun testPathParameterWithNamedTailcard() = runTest {
        SimpleRoute("/one/{two}/{three...}").apply {
            directions()
                .pathParameter("two", "two")
                .shouldBe("/one/two")
            directions()
                .pathParameter("two", "two")
                .pathParameter("three", "three")
                .shouldBe("/one/two/three")
            directions()
                .pathParameter("two", "two")
                .pathParameter("three", "three", "four", "five")
                .shouldBe("/one/two/three/four/five")

            directions()
                .path("two", "three", "four", "five")
                .shouldBe("/one/two/three/four/five")
        }
    }

// Path Tests - Failure
// ---------------------------------------------------------------------------------------------------------------------

    @Test
    fun testFailureWithNoPathParametersAllowed() = runTest {
        SimpleRoute("/").apply {
            directions()
                .pathParameter("two", "two")
                .shouldFail()
        }
    }

    @Test
    fun testFailureWithRequiredPathParameter() = runTest {
        SimpleRoute("/one/:two").apply {
            directions()
                .shouldFail()
            directions()
                .pathParameter("two", "two", ":three")
                .shouldFail()
        }
    }

    @Test
    fun testPathWildcard() = runTest {
        SimpleRoute("/one/*").apply {
            directions()
                .shouldFail()
            directions()
                .pathParameter("two", "three")
                .shouldFail()
        }
    }

// Query Parameter Tests - Success
// ---------------------------------------------------------------------------------------------------------------------

    @Test
    fun test1StaticQuery() = runTest {
        SimpleRoute("/one?one=two").apply {
            directions()
                .shouldBe("/one?one=two")
        }
    }

    @Test
    fun testMultipleStaticQuery() = runTest {
        SimpleRoute("/one?one=two&three=four").apply {
            directions()
                .shouldBe("/one?one=two&three=four")
        }
    }

    @Test
    fun testRequiredQueryParameterWith1Value() = runTest {
        SimpleRoute("/one?one={!}").apply {
            directions()
                .queryParameter("one", "two")
                .shouldBe("/one?one=two")
        }
    }

    @Test
    fun testRequiredQueryParameterWithMultipleValues() = runTest {
        SimpleRoute("/one?one={[!]}").apply {
            directions()
                .queryParameter("one", "two")
                .shouldBe("/one?one=two")
            directions()
                .queryParameter("one", "two", "three", "four")
                .shouldBe("/one?one=two&one=three&one=four")
        }
    }

    @Test
    fun testOptionalQueryParameterWith1Value() = runTest {
        SimpleRoute("/one?one={?}").apply {
            directions().shouldBe("/one")
            directions()
                .queryParameter("one", "two")
                .shouldBe("/one?one=two")
        }
    }

    @Test
    fun testOptionalQueryParameterWithMultipleValues() = runTest {
        SimpleRoute("/one?one={[?]}").apply {
            directions().shouldBe("/one")
            directions()
                .queryParameter("one", "two")
                .shouldBe("/one?one=two")
            directions()
                .queryParameter("one", "two", "three", "four")
                .shouldBe("/one?one=two&one=three&one=four")
        }
    }

    @Test
    fun testRemainder() = runTest {
        SimpleRoute("/one?{...}").apply {
            directions()
                .shouldBe("/one")
            directions()
                .queryParameter("one", "two")
                .shouldBe("/one?one=two")
            directions()
                .queryParameter("one", "two", "three", "four")
                .shouldBe("/one?one=two&one=three&one=four")
            directions()
                .queryParameter("one", "two", "three", "four")
                .queryParameter("two", "TWO", "THREE", "FOUR")
                .shouldBe("/one?one=two&one=three&one=four&two=TWO&two=THREE&two=FOUR")
        }
    }

// Query Parameter Tests - Failure
// ---------------------------------------------------------------------------------------------------------------------

    @Test
    fun testFailureWithNoQuery() = runTest {
        SimpleRoute("/one").apply {
            directions()
                .queryParameter("one", "two")
                .shouldFail()
        }
    }

    @Test
    fun testFailureWith1StaticQuery() = runTest {
        SimpleRoute("/one?one=two").apply {
            directions()
                .queryParameter("one", "two")
                .shouldFail()
        }
    }

    @Test
    fun testFailureWithRequiredQueryParameterWith1Value() = runTest {
        SimpleRoute("/one?one={!}").apply {
            directions()
                .shouldFail()
            directions()
                .queryParameter("two", "three")
                .shouldFail()
            directions()
                .queryParameter("one", "two", "three")
                .shouldFail()
        }
    }

    @Test
    fun testFailureWithRequiredQueryParameterWithMultipleValue() = runTest {
        SimpleRoute("/one?one={[!]}").apply {
            directions()
                .shouldFail()
            directions()
                .queryParameter("two", "three")
                .shouldFail()
        }
    }

    @Test
    fun testFailureWithOptionalQueryParameterWith1Value() = runTest {
        SimpleRoute("/one?one={?}").apply {
            directions()
                .queryParameter("two", "three")
                .shouldFail()
            directions()
                .queryParameter("one", "two", "three")
                .shouldFail()
        }
    }

    @Test
    fun testFailureWithOptionalQueryParameterWithMultipleValue() = runTest {
        SimpleRoute("/one?one={[?]}").apply {
            directions()
                .queryParameter("two", "three")
                .shouldFail()
        }
    }

// Test encoding
// ---------------------------------------------------------------------------------------------------------------------

    @Test
    fun testEncoding() = runTest {
        SimpleRoute("/one/:two?three={?}").apply {
            directions()
                .pathParameter("two", "a b")
                .queryParameter("three", "c & d")
                .shouldBe("/one/a%20b?three=c+%26+d")
            directions()
                .pathParameter("two", "a/b")
                .queryParameter("three", "c/&/d")
                .shouldBe("/one/a%2Fb?three=c%2F%26%2Fd")
        }
    }

    companion object {
        private fun <T : Route> Destination.Directions<T>.shouldBe(expectedDestinationUrl: String) {
            val generatedDestinationUrl = build()

            // check that the generated directions is what we expect, as set in the test case
            assertEquals(expectedDestinationUrl, generatedDestinationUrl)

            // check that the directions generated by a route will also be able to be matched by that same route
            val match = route.matcher.matchDestination(route, UnmatchedDestination.parse(generatedDestinationUrl))
            assertTrue { match is Destination.Match<T> }
        }

        private fun <T : Route> Destination.Directions<T>.shouldFail() {
            assertFails {
                build()
            }
        }
    }
}
