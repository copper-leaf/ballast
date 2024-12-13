@file:Suppress("USELESS_IS_CHECK")

package com.copperleaf.ballast.examples.navigation

import com.copperleaf.ballast.eventHandler
import com.copperleaf.ballast.navigation.routing.Destination
import com.copperleaf.ballast.navigation.routing.RouterContract
import com.copperleaf.ballast.navigation.routing.RoutingTable
import com.copperleaf.ballast.navigation.routing.currentDestinationOrNull
import com.copperleaf.ballast.navigation.routing.fromEnum
import com.copperleaf.ballast.navigation.vm.RouterInputHandlerImpl
import com.copperleaf.ballast.test.viewModelTest
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class NavigationTest {
    @Test
    fun doTest() = runTest {
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
                    assertNotNull(currentRoute)
                    assertTrue(currentRoute is Destination.Match<AppScreenRoute>)
                    assertTrue(currentRoute.originalRoute is AppScreenRoute)
                    assertEquals<Any?>("/Home", currentRoute.originalDestinationUrl)
                    assertEquals(Home, currentRoute.matchRoute<AppScreen>(currentRoute.originalRoute))
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
                    assertNotNull(currentRoute)
                    assertTrue(currentRoute is Destination.Match<AppScreenRoute>)
                    assertTrue(currentRoute.originalRoute is AppScreenRoute)
                    assertEquals<Any?>("/blog", currentRoute.originalDestinationUrl)
                    assertEquals(PostList(null), currentRoute.matchRoute<AppScreen>(currentRoute.originalRoute))
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
                    assertNotNull(currentRoute)
                    assertTrue(currentRoute is Destination.Match<AppScreenRoute>)
                    assertTrue(currentRoute.originalRoute is AppScreenRoute)
                    assertEquals<Any?>("/blog?sort=desc", currentRoute.originalDestinationUrl)
                    assertEquals(PostList("desc"), currentRoute.matchRoute<AppScreen>(currentRoute.originalRoute))
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
                    assertNotNull(currentRoute)
                    assertTrue(currentRoute is Destination.Match<AppScreenRoute>)
                    assertTrue(currentRoute.originalRoute is AppScreenRoute)
                    assertEquals<Any?>("/blog/5", currentRoute.originalDestinationUrl)
                    assertEquals(PostDetails(5), currentRoute.matchRoute<AppScreen>(currentRoute.originalRoute))
                }
            }
        }
    }
}
