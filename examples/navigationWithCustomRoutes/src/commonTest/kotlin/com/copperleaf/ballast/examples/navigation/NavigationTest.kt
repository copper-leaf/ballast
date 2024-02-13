package com.copperleaf.ballast.examples.navigation

import com.copperleaf.ballast.eventHandler
import com.copperleaf.ballast.navigation.routing.Destination
import com.copperleaf.ballast.navigation.routing.RouterContract
import com.copperleaf.ballast.navigation.routing.RoutingTable
import com.copperleaf.ballast.navigation.routing.currentDestinationOrNull
import com.copperleaf.ballast.navigation.routing.fromEnum
import com.copperleaf.ballast.navigation.vm.RouterInputHandlerImpl
import com.copperleaf.ballast.test.viewModelTest
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf

class NavigationTest : StringSpec({
    "doTest" {
        viewModelTest(
            inputHandler = RouterInputHandlerImpl<AppScreenRoute>(),
            eventHandler = eventHandler { },
        ) {
            defaultInitialState { RouterContract.State(routingTable = RoutingTable.fromEnum(AppScreenRoute.entries)) }

            scenario("Test Home Route") {
                running {
                    +RouterContract.Inputs.GoToDestination<AppScreenRoute>(
                        destination = Home.navigate()
                    )
                }
                resultsIn {
                    val currentRoute = latestState.currentDestinationOrNull
                    currentRoute.shouldNotBeNull()
                    currentRoute.shouldBeInstanceOf<Destination.Match<AppScreenRoute>>()
                    currentRoute.originalRoute.shouldBeInstanceOf<AppScreenRoute>()
                    currentRoute.originalDestinationUrl shouldBe "/Home"
                    currentRoute.matchRoute<AppScreenRoute>(currentRoute.originalRoute) shouldBe Home
                }
            }

            scenario("Test Post List (no query))") {
                running {
                    +RouterContract.Inputs.GoToDestination<AppScreenRoute>(
                        destination = PostList.navigate(null)
                    )
                }
                resultsIn {
                    val currentRoute = latestState.currentDestinationOrNull
                    currentRoute.shouldNotBeNull()
                    currentRoute.shouldBeInstanceOf<Destination.Match<AppScreenRoute>>()
                    currentRoute.originalRoute.shouldBeInstanceOf<AppScreenRoute>()
                    currentRoute.originalDestinationUrl shouldBe "/blog"
                    currentRoute.matchRoute<AppScreenRoute>(currentRoute.originalRoute) shouldBe PostList(null)
                }
            }

            scenario("Test Post List Route") {
                running {
                    +RouterContract.Inputs.GoToDestination<AppScreenRoute>(
                        destination = PostList.navigate("desc")
                    )
                }
                resultsIn {
                    val currentRoute = latestState.currentDestinationOrNull
                    currentRoute.shouldNotBeNull()
                    currentRoute.shouldBeInstanceOf<Destination.Match<AppScreenRoute>>()
                    currentRoute.originalRoute.shouldBeInstanceOf<AppScreenRoute>()
                    currentRoute.originalDestinationUrl shouldBe "/blog?sort=desc"
                    currentRoute.matchRoute<AppScreenRoute>(currentRoute.originalRoute) shouldBe PostList("desc")
                }
            }

            scenario("Test Post") {
                running {
                    +RouterContract.Inputs.GoToDestination<AppScreenRoute>(
                        destination = PostDetails.navigate(5)
                    )
                }
                resultsIn {
                    val currentRoute = latestState.currentDestinationOrNull
                    currentRoute.shouldNotBeNull()
                    currentRoute.shouldBeInstanceOf<Destination.Match<AppScreenRoute>>()
                    currentRoute.originalRoute.shouldBeInstanceOf<AppScreenRoute>()
                    currentRoute.originalDestinationUrl shouldBe "/blog/5"
                    currentRoute.matchRoute<AppScreenRoute>(currentRoute.originalRoute) shouldBe PostDetails(5)
                }
            }
        }
    }
})
