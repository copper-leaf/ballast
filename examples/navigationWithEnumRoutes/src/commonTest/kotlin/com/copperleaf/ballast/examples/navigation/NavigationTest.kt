package com.copperleaf.ballast.examples.navigation

import com.copperleaf.ballast.eventHandler
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
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class NavigationTest {
    @Test
    fun doTest() = runTest {
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
                    assertNotNull(currentRoute)
                    assertEquals<Any?>(AppScreen.Home, currentRoute.originalRoute)
                    assertEquals<Any?>("/app/home", currentRoute.originalDestinationUrl)
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
                    assertNotNull(currentRoute)
                    assertEquals<Any?>(AppScreen.PostList, currentRoute.originalRoute)
                    assertEquals<Any?>("/app/posts", currentRoute.originalDestinationUrl)
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
                    assertNotNull(currentRoute)
                    assertEquals<Any?>(AppScreen.PostList, currentRoute.originalRoute)
                    assertEquals<Any?>("/app/posts?sort=desc", currentRoute.originalDestinationUrl)
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
                    assertNotNull(currentRoute)
                    assertEquals<Any?>(AppScreen.PostDetails, currentRoute.originalRoute)
                    assertEquals<Any?>("/app/posts/5", currentRoute.originalDestinationUrl)
                }
            }
        }
    }
}
