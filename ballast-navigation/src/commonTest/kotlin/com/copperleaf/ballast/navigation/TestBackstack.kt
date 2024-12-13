package com.copperleaf.ballast.navigation

import com.copperleaf.ballast.navigation.internal.BackstackNavigatorImpl
import com.copperleaf.ballast.navigation.routing.BackstackNavigator
import com.copperleaf.ballast.navigation.routing.Destination
import com.copperleaf.ballast.navigation.routing.RouterContract
import com.copperleaf.ballast.navigation.routing.Tag
import com.copperleaf.ballast.navigation.routing.addToTop
import com.copperleaf.ballast.navigation.routing.goBack
import com.copperleaf.ballast.navigation.routing.popAllWithAnnotation
import com.copperleaf.ballast.navigation.routing.popUntil
import com.copperleaf.ballast.navigation.routing.popUntilRoute
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class TestBackstack {
    fun runBackstackTest(
        originalBackstack: List<String>,
        expectedResult: List<String>,
        operation: BackstackNavigator<SimpleRoute>.() -> Unit,
    ) {
        val originalState = RouterContract.State(
            routingTable = MatchAllRoutingTable(),
            backstack = originalBackstack.map { Destination.Match(it, SimpleRoute(it)) },
        )

        val actualResult = BackstackNavigatorImpl(originalState, Navigate(operation)).applyUpdate()
        val actualResultWithoutAnnotations = actualResult
            .map {
                Destination.Match(it.originalDestinationUrl, SimpleRoute(it.originalDestinationUrl))
            }
        val expectedResultAsMatches = expectedResult.map { Destination.Match(it, SimpleRoute(it)) }

        assertEquals<Any?>(expectedResultAsMatches, actualResultWithoutAnnotations)
    }

    @Test
    fun noChange() = runTest {
        runBackstackTest(
            originalBackstack = listOf("/one", "/two", "/three"),
            expectedResult = listOf("/one", "/two", "/three"),
        ) {

        }
    }

    @Test
    fun goBack() = runTest {
        runBackstackTest(
            originalBackstack = listOf("/one", "/two", "/three"),
            expectedResult = listOf("/one", "/two"),
        ) {
            goBack(1)
        }
    }

    @Test
    fun goBackMultipleCalls() = runTest {
        runBackstackTest(
            originalBackstack = listOf("/one", "/two", "/three"),
            expectedResult = listOf("/one"),
        ) {
            goBack(1)
            goBack(1)
        }
    }

    @Test
    fun goBack2StepsInOneCall() = runTest {
        runBackstackTest(
            originalBackstack = listOf("/one", "/two", "/three"),
            expectedResult = listOf("/one"),
        ) {
            goBack(2)
        }
    }

    @Test
    fun addToTop() = runTest {
        runBackstackTest(
            originalBackstack = listOf("/one", "/two", "/three"),
            expectedResult = listOf("/one", "/two", "/three", "/four"),
        ) {
            addToTop("/four", emptySet())
        }
    }

    @Test
    fun replaceTop() = runTest {
        runBackstackTest(
            originalBackstack = listOf("/one", "/two", "/three"),
            expectedResult = listOf("/one", "/two", "/four"),
        ) {
            goBack(1)
            addToTop("/four", emptySet())
        }
    }

    @Test
    fun popWithAannotation() = runTest {
        runBackstackTest(
            originalBackstack = listOf("/one", "/two", "/three"),
            expectedResult = listOf("/one", "/two", "/three", "/four"),
        ) {
            addToTop("/four", emptySet())
            addToTop("/five", setOf(Tag("1")))
            addToTop("/six", setOf(Tag("1")))
            addToTop("/seven", setOf(Tag("1")))

            popAllWithAnnotation(Tag("1"))
        }
    }

    @Test
    fun popWithAnnotationMultipleTags() = runTest {
        runBackstackTest(
            originalBackstack = listOf("/one", "/two", "/three"),
            expectedResult = listOf("/one", "/two", "/three", "/four", "/five"),
        ) {
            addToTop("/four", emptySet())
            addToTop("/five", setOf(Tag("1")))
            addToTop("/six", setOf(Tag("2")))
            addToTop("/seven", setOf(Tag("2")))

            popAllWithAnnotation(Tag("2"))
        }
    }

    @Test
    fun popUntilInclusive() = runTest {
        runBackstackTest(
            originalBackstack = listOf("/one", "/two", "/three"),
            expectedResult = listOf("/one"),
        ) {
            popUntil(inclusive = true) { it.originalDestinationUrl == "/two" }
        }
    }

    @Test
    fun popUntilExclusive() = runTest {
        runBackstackTest(
            originalBackstack = listOf("/one", "/two", "/three"),
            expectedResult = listOf("/one", "/two"),
        ) {
            popUntil(inclusive = false) { it.originalDestinationUrl == "/two" }
        }
    }

    @Test
    fun popUntilInclusiveFirstElement() = runTest {
        runBackstackTest(
            originalBackstack = listOf("/one", "/two", "/three"),
            expectedResult = emptyList(),
        ) {
            popUntil(inclusive = true) { it.originalDestinationUrl == "/one" }
        }
    }

    @Test
    fun popUntilExclusiveFirstElement() = runTest {
        runBackstackTest(
            originalBackstack = listOf("/one", "/two", "/three"),
            expectedResult = listOf("/one"),
        ) {
            popUntil(inclusive = false) { it.originalDestinationUrl == "/one" }
        }
    }

    @Test
    fun popUntilInclusiveLastElement() = runTest {
        runBackstackTest(
            originalBackstack = listOf("/one", "/two", "/three"),
            expectedResult = listOf("/one", "/two"),
        ) {
            popUntil(inclusive = true) { it.originalDestinationUrl == "/three" }
        }
    }

    @Test
    fun popUntilExclusiveLastElement() = runTest {
        runBackstackTest(
            originalBackstack = listOf("/one", "/two", "/three"),
            expectedResult = listOf("/one", "/two", "/three"),
        ) {
            popUntil(inclusive = false) { it.originalDestinationUrl == "/three" }
        }
    }

    @Test
    fun popUntilInclusiveNoMatch() = runTest {
        runBackstackTest(
            originalBackstack = listOf("/one", "/two", "/three"),
            expectedResult = emptyList(),
        ) {
            popUntil(inclusive = true) { it.originalDestinationUrl == "/four" }
        }
    }

    @Test
    fun popUntilExclusiveNoMatch() = runTest {
        runBackstackTest(
            originalBackstack = listOf("/one", "/two", "/three"),
            expectedResult = emptyList(),
        ) {
            popUntil(inclusive = false) { it.originalDestinationUrl == "/four" }
        }
    }

    @Test
    fun popUntilRoute() = runTest {
        runBackstackTest(
            originalBackstack = listOf("/one", "/two", "/three"),
            expectedResult = listOf("/one", "/two"),
        ) {
            popUntilRoute(inclusive = false, route = SimpleRoute("/two"))
        }
    }
}
