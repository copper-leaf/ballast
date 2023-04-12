package com.copperleaf.ballast.navigation

import com.copperleaf.ballast.navigation.Assertions.assertEquals
import com.copperleaf.ballast.navigation.Assertions.assertFails
import com.copperleaf.ballast.navigation.Assertions.assertTrue
import com.copperleaf.ballast.navigation.routing.Destination
import com.copperleaf.ballast.navigation.routing.Route
import com.copperleaf.ballast.navigation.routing.UnmatchedDestination
import com.copperleaf.ballast.navigation.routing.build
import com.copperleaf.ballast.navigation.routing.directions
import com.copperleaf.ballast.navigation.routing.matchDestination
import com.copperleaf.ballast.navigation.routing.path
import com.copperleaf.ballast.navigation.routing.pathParameter
import com.copperleaf.ballast.navigation.routing.queryParameter
import io.kotest.core.spec.style.StringSpec

class TestRouteDirections : StringSpec({
// Path Tests - Success
// ---------------------------------------------------------------------------------------------------------------------

    "testNoPathParametersAllowed()" {
        SimpleRoute("/").apply {
            directions()
                .shouldBe("/")
        }
        SimpleRoute("/one").apply {
            directions()
                .shouldBe("/one")
        }
    }

    "test1RequiredPathParameter()" {
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

    "test1OptionalPathParameter()" {
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

    "testMultiplePathParameters()" {
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

    "testPathAnonymousTailcard()" {
        SimpleRoute("/one/{...}").apply {
            directions()
                .shouldBe("/one")
        }
    }

    "testPathNamedTailcard()" {
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

    "testPathParameterWithAnonymousTailcard()" {
        SimpleRoute("/one/{two}/{...}").apply {
            directions()
                .pathParameter("two", "three")
                .shouldBe("/one/three")
        }
    }

    "testPathParameterWithNamedTailcard()" {
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

    "testFailureWithNoPathParametersAllowed()" {
        SimpleRoute("/").apply {
            directions()
                .pathParameter("two", "two")
                .shouldFail()
        }
    }

    "testFailureWithRequiredPathParameter()" {
        SimpleRoute("/one/:two").apply {
            directions()
                .shouldFail()
            directions()
                .pathParameter("two", "two", ":three")
                .shouldFail()
        }
    }

    "testPathWildcard()" {
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

    "test1StaticQuery()" {
        SimpleRoute("/one?one=two").apply {
            directions()
                .shouldBe("/one?one=two")
        }
    }

    "testMultipleStaticQuery()" {
        SimpleRoute("/one?one=two&three=four").apply {
            directions()
                .shouldBe("/one?one=two&three=four")
        }
    }

    "testRequiredQueryParameterWith1Value()" {
        SimpleRoute("/one?one={!}").apply {
            directions()
                .queryParameter("one", "two")
                .shouldBe("/one?one=two")
        }
    }

    "testRequiredQueryParameterWithMultipleValues()" {
        SimpleRoute("/one?one={[!]}").apply {
            directions()
                .queryParameter("one", "two")
                .shouldBe("/one?one=two")
            directions()
                .queryParameter("one", "two", "three", "four")
                .shouldBe("/one?one=two&one=three&one=four")
        }
    }

    "testOptionalQueryParameterWith1Value()" {
        SimpleRoute("/one?one={?}").apply {
            directions().shouldBe("/one")
            directions()
                .queryParameter("one", "two")
                .shouldBe("/one?one=two")
        }
    }

    "testOptionalQueryParameterWithMultipleValues()" {
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

    "testRemainder()" {
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

    "testFailureWithNoQuery()" {
        SimpleRoute("/one").apply {
            directions()
                .queryParameter("one", "two")
                .shouldFail()
        }
    }

    "testFailureWith1StaticQuery()" {
        SimpleRoute("/one?one=two").apply {
            directions()
                .queryParameter("one", "two")
                .shouldFail()
        }
    }

    "testFailureWithRequiredQueryParameterWith1Value()" {
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

    "testFailureWithRequiredQueryParameterWithMultipleValue()" {
        SimpleRoute("/one?one={[!]}").apply {
            directions()
                .shouldFail()
            directions()
                .queryParameter("two", "three")
                .shouldFail()
        }
    }

    "testFailureWithOptionalQueryParameterWith1Value()" {
        SimpleRoute("/one?one={?}").apply {
            directions()
                .queryParameter("two", "three")
                .shouldFail()
            directions()
                .queryParameter("one", "two", "three")
                .shouldFail()
        }
    }

    "testFailureWithOptionalQueryParameterWithMultipleValue()" {
        SimpleRoute("/one?one={[?]}").apply {
            directions()
                .queryParameter("two", "three")
                .shouldFail()
        }
    }

// Test encoding
// ---------------------------------------------------------------------------------------------------------------------

    "testEncoding()" {
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
}) {
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
