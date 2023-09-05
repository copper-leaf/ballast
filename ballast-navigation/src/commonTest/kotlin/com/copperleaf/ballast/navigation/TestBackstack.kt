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
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class TestBackstack : StringSpec({
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

        actualResultWithoutAnnotations shouldBe expectedResultAsMatches
    }

    "no change" {
        runBackstackTest(
            originalBackstack = listOf("/one", "/two", "/three"),
            expectedResult = listOf("/one", "/two", "/three"),
        ) {

        }
    }

    "goBack" {
        runBackstackTest(
            originalBackstack = listOf("/one", "/two", "/three"),
            expectedResult = listOf("/one", "/two"),
        ) {
            goBack(1)
        }
    }
    "goBack multiple calls" {
        runBackstackTest(
            originalBackstack = listOf("/one", "/two", "/three"),
            expectedResult = listOf("/one"),
        ) {
            goBack(1)
            goBack(1)
        }
    }
    "goBack 2 steps in one call" {
        runBackstackTest(
            originalBackstack = listOf("/one", "/two", "/three"),
            expectedResult = listOf("/one"),
        ) {
            goBack(2)
        }
    }

    "addToTop" {
        runBackstackTest(
            originalBackstack = listOf("/one", "/two", "/three"),
            expectedResult = listOf("/one", "/two", "/three", "/four"),
        ) {
            addToTop("/four", emptySet())
        }
    }
    "replaceTop" {
        runBackstackTest(
            originalBackstack = listOf("/one", "/two", "/three"),
            expectedResult = listOf("/one", "/two", "/four"),
        ) {
            goBack(1)
            addToTop("/four", emptySet())
        }
    }
    "popWith annotation" {
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

    "popWith annotation multiple tags" {
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

    "popUntil inclusive" {
        runBackstackTest(
            originalBackstack = listOf("/one", "/two", "/three"),
            expectedResult = listOf("/one"),
        ) {
            popUntil(inclusive = true) { it.originalDestinationUrl == "/two" }
        }
    }
    "popUntil exclusive" {
        runBackstackTest(
            originalBackstack = listOf("/one", "/two", "/three"),
            expectedResult = listOf("/one", "/two"),
        ) {
            popUntil(inclusive = false) { it.originalDestinationUrl == "/two" }
        }
    }

    "popUntil inclusive first element" {
        runBackstackTest(
            originalBackstack = listOf("/one", "/two", "/three"),
            expectedResult = emptyList(),
        ) {
            popUntil(inclusive = true) { it.originalDestinationUrl == "/one" }
        }
    }
    "popUntil exclusive first element" {
        runBackstackTest(
            originalBackstack = listOf("/one", "/two", "/three"),
            expectedResult = listOf("/one"),
        ) {
            popUntil(inclusive = false) { it.originalDestinationUrl == "/one" }
        }
    }

    "popUntil inclusive last element" {
        runBackstackTest(
            originalBackstack = listOf("/one", "/two", "/three"),
            expectedResult = listOf("/one", "/two"),
        ) {
            popUntil(inclusive = true) { it.originalDestinationUrl == "/three" }
        }
    }
    "popUntil exclusive last element" {
        runBackstackTest(
            originalBackstack = listOf("/one", "/two", "/three"),
            expectedResult = listOf("/one", "/two", "/three"),
        ) {
            popUntil(inclusive = false) { it.originalDestinationUrl == "/three" }
        }
    }

    "popUntil inclusive no match" {
        runBackstackTest(
            originalBackstack = listOf("/one", "/two", "/three"),
            expectedResult = emptyList(),
        ) {
            popUntil(inclusive = true) { it.originalDestinationUrl == "/four" }
        }
    }
    "popUntil exclusive no match" {
        runBackstackTest(
            originalBackstack = listOf("/one", "/two", "/three"),
            expectedResult = emptyList(),
        ) {
            popUntil(inclusive = false) { it.originalDestinationUrl == "/four" }
        }
    }

    "popUntilRoute" {
        runBackstackTest(
            originalBackstack = listOf("/one", "/two", "/three"),
            expectedResult = listOf("/one", "/two"),
        ) {
            popUntilRoute(inclusive = false, route = SimpleRoute("/two"))
        }
    }
})
