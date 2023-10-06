package com.copperleaf.ballast.examples.navigation

import com.copperleaf.ballast.eventHandler
import com.copperleaf.ballast.navigation.routing.Destination
import com.copperleaf.ballast.navigation.routing.RouterContract
import com.copperleaf.ballast.navigation.routing.RoutingTable
import com.copperleaf.ballast.navigation.routing.build
import com.copperleaf.ballast.navigation.routing.currentDestinationOrNull
import com.copperleaf.ballast.navigation.routing.directions
import com.copperleaf.ballast.navigation.routing.fromEnum
import com.copperleaf.ballast.navigation.routing.pathParameter
import com.copperleaf.ballast.navigation.routing.queryParameter
import com.copperleaf.ballast.navigation.vm.RouterInputHandlerImpl
import com.copperleaf.ballast.test.viewModelTest
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf

class NavigationTest : StringSpec({
    "doTest" {
        viewModelTest(
            inputHandler = RouterInputHandlerImpl<AppScreen>(),
            eventHandler = eventHandler { },
        ) {
            defaultInitialState { RouterContract.State(routingTable = RoutingTable.fromEnum(AppScreen.entries)) }

            scenario("Test Home Route") {
                running {
                    +RouterContract.Inputs.GoToDestination<AppScreen>(
                        destination = AppScreen.Home.directions().build()
                    )
                }
                resultsIn {
                    val currentRoute = latestState.currentDestinationOrNull
                    currentRoute.shouldNotBeNull()
                    currentRoute.shouldBeInstanceOf<Destination.Match<AppScreen>>()
                    currentRoute.originalRoute shouldBe AppScreen.Home
                    currentRoute.originalDestinationUrl shouldBe "/app/home"
                }
            }

            scenario("Test Post List (no query))") {
                running {
                    +RouterContract.Inputs.GoToDestination<AppScreen>(
                        destination = AppScreen.PostList.directions().build()
                    )
                }
                resultsIn {
                    val currentRoute = latestState.currentDestinationOrNull
                    currentRoute.shouldNotBeNull()
                    currentRoute.shouldBeInstanceOf<Destination.Match<AppScreen>>()
                    currentRoute.originalRoute shouldBe AppScreen.PostList
                    currentRoute.originalDestinationUrl shouldBe "/app/posts"
                }
            }

            scenario("Test Post List Route") {
                running {
                    +RouterContract.Inputs.GoToDestination<AppScreen>(
                        destination = AppScreen.PostList.directions().queryParameter("sort", "desc").build()
                    )
                }
                resultsIn {
                    val currentRoute = latestState.currentDestinationOrNull
                    currentRoute.shouldNotBeNull()
                    currentRoute.shouldBeInstanceOf<Destination.Match<AppScreen>>()
                    currentRoute.originalRoute shouldBe AppScreen.PostList
                    currentRoute.originalDestinationUrl shouldBe "/app/posts?sort=desc"
                }
            }

            scenario("Test Post") {
                running {
                    +RouterContract.Inputs.GoToDestination<AppScreen>(
                        destination = AppScreen.PostDetails.directions().pathParameter("postId", "5").build()
                    )
                }
                resultsIn {
                    val currentRoute = latestState.currentDestinationOrNull
                    currentRoute.shouldNotBeNull()
                    currentRoute.shouldBeInstanceOf<Destination.Match<AppScreen>>()
                    currentRoute.originalRoute shouldBe AppScreen.PostDetails
                    currentRoute.originalDestinationUrl shouldBe "/app/posts/5"
                }
            }
        }
    }
})
