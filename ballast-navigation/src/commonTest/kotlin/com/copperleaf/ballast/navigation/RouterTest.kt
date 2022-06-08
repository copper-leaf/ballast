package com.copperleaf.ballast.navigation

import com.copperleaf.ballast.navigation.routing.Route
import com.copperleaf.ballast.navigation.routing.directions
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class RouterTest {

    @Test
    fun testRouteDirections() {
        Route("/one").let { route ->
            val oneQueryParamter_simple = mapOf("asdf" to listOf("123"))
            val oneQueryParamter_encoded = mapOf("as df" to listOf("12 34"))
            val twoQueryParamters_simple = mapOf("asdf" to listOf("123"), "qwerty" to listOf("456"))
            val twoQueryParamters_encoded = mapOf("as df" to listOf("12 34"), "qwe rty" to listOf("56 78"))
            val oneQueryString_simple = "asdf=123"
            val oneQueryString_encoded = "as%20df=12%2034"
            val twoQueryString_simple = "asdf=123&qwerty=456"
            val twoQueryString_encoded = "as%20df=12%2034&qwe%20rty=56%2078"
            assertEquals("/one", route.directions())
            assertEquals("/one?$oneQueryString_simple", route.directions(queryParameters = oneQueryParamter_simple))
            assertEquals("/one?$oneQueryString_encoded", route.directions(queryParameters = oneQueryParamter_encoded))
            assertEquals("/one?$twoQueryString_simple", route.directions(queryParameters = twoQueryParamters_simple))
            assertEquals("/one?$twoQueryString_encoded", route.directions(queryParameters = twoQueryParamters_encoded))
        }
        Route("/one/:two").let { route ->
            assertEquals("/one/three", route.directions(pathParameters = mapOf("two" to listOf("three"))))
            assertEquals("/one/th%20ree", route.directions(pathParameters = mapOf("two" to listOf("th ree"))))
            assertFailsWith<Throwable> { route.directions() }.let {
                assertEquals("Non-optional path parameter 'two' must be provided in destination to route '/one/:two'", it.message)
            }
            assertFailsWith<Throwable> { route.directions(pathParameters = mapOf("three" to listOf("four"))) }.let {
                assertEquals("The following path parameter values could not be found in the directions to route '/one/:two': [three]", it.message)
            }
        }
        Route("/one/{two?}").let { route ->
            assertEquals("/one", route.directions())
            assertEquals("/one/three", route.directions(pathParameters = mapOf("two" to listOf("three"))))
            assertFailsWith<Throwable> { route.directions(pathParameters = mapOf("three" to listOf("four"))) }.let {
                assertEquals("The following path parameter values could not be found in the directions to route '/one/{two?}': [three]", it.message)
            }
        }
        Route("/one/*").let { route ->
            assertFailsWith<Throwable> { route.directions() }.let {
                assertEquals("Cannot create directions for wildcard path segment, consider switching to a named parameter instead in route '/one/*'", it.message)
            }
        }
        Route("/one/{...}").let { route ->
            assertFailsWith<Throwable> { route.directions() }.let {
                assertEquals("Cannot create directions for unnamed tailcard path segment, consider switching to a named tailcard instead in route '/one/{...}'", it.message)
            }
        }
        Route("/one/{two...}").let { route ->
            assertEquals("/one", route.directions())
            assertEquals("/one/two", route.directions(pathParameters = mapOf("two" to listOf("two"))))
            assertEquals("/one/two/three", route.directions(pathParameters = mapOf("two" to listOf("two", "three"))))
            assertEquals("/one/a/b/c/d/e/f/g", route.directions(pathParameters = mapOf("two" to listOf("a", "b ", " c", "d", "e", "f", "g"))))
        }
    }
}
