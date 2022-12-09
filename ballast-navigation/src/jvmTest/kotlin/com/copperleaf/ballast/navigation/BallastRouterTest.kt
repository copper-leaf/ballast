package com.copperleaf.ballast.navigation

import com.copperleaf.ballast.ExperimentalBallastApi
import com.copperleaf.ballast.eventHandler
import com.copperleaf.ballast.navigation.routing.Destination
import com.copperleaf.ballast.navigation.routing.RouterContract
import com.copperleaf.ballast.navigation.routing.RoutingTable
import com.copperleaf.ballast.navigation.routing.currentDestinationOrNotFound
import com.copperleaf.ballast.navigation.routing.currentDestinationOrNull
import com.copperleaf.ballast.navigation.routing.currentDestinationOrThrow
import com.copperleaf.ballast.navigation.routing.fromEnum
import com.copperleaf.ballast.navigation.vm.RouterInputHandlerImpl
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertNull
import kotlin.time.ExperimentalTime

@ExperimentalTime
@ExperimentalCoroutinesApi
@ExperimentalBallastApi
class BallastRouterTest {

    @Test
    fun doTest() =
        runBlockingViewModelTest<RouterContract.Inputs<TestRoutes>, RouterContract.Events<TestRoutes>, RouterContract.State<TestRoutes>>(
            inputHandler = RouterInputHandlerImpl<TestRoutes>(),
            eventHandler = eventHandler<RouterContract.Inputs<TestRoutes>, RouterContract.Events<TestRoutes>, RouterContract.State<TestRoutes>> { },
        ) {
            defaultInitialState { RouterContract.State(routingTable = RoutingTable.fromEnum(TestRoutes.values())) }

            val destinationForCreate = Destination.Match(
                "/test/new",
                TestRoutes.Create,
            )
            val destinationForDetails = Destination.Match(
                "/test/12345",
                TestRoutes.Details,
                pathParameters = mapOf("testId" to listOf("12345"))
            )
            val destinationForList = Destination.Match(
                "/test?pageSize=25&page=2",
                TestRoutes.ListAll,
                queryParameters = mapOf("pageSize" to listOf("25"), "page" to listOf("2"))
            )
            val destinationForListWithSort = Destination.Match(
                "/test?sort=asc&pageSize=25&page=2",
                TestRoutes.ListAll,
                queryParameters = mapOf("pageSize" to listOf("25"), "page" to listOf("2"), "sort" to listOf("asc"))
            )

            scenario("simple navigation test") {
                running {
                    +RouterContract.Inputs.GoToDestination<TestRoutes>("/test/new")
                    +RouterContract.Inputs.GoToDestination<TestRoutes>("/test/12345")
                }
                resultsIn {
                    assertEquals(
                        destinationForDetails,
                        latestState.backstack.currentDestinationOrNotFound
                    )
                    assertEquals(
                        listOf(
                            RouterContract.Events.BackstackChanged(
                                listOf(
                                    destinationForCreate,
                                ),
                            ),
                            RouterContract.Events.BackstackChanged(
                                listOf(
                                    destinationForCreate,
                                    destinationForDetails,
                                ),
                            )
                        ),
                        this.events,
                    )
                }
            }
            scenario("navigation not found") {
                running {
                    +RouterContract.Inputs.GoToDestination<TestRoutes>("/notTesting")
                    +RouterContract.Inputs.GoToDestination<TestRoutes>("/notTesting/two")
                }
                resultsIn {
                    assertEquals(
                        Destination.Mismatch("/notTesting/two"),
                        latestState.backstack.currentDestinationOrNotFound
                    )
                    assertNull(latestState.backstack.currentDestinationOrNull)
                    assertFails { latestState.backstack.currentDestinationOrThrow }
                    assertEquals(
                        listOf(Destination.Mismatch("/notTesting/two")),
                        latestState.backstack
                    )

                }
            }
            scenario("forward and back") {
                running {
                    +RouterContract.Inputs.GoToDestination<TestRoutes>("/test/new")
                    +RouterContract.Inputs.GoToDestination<TestRoutes>("/test/12345")
                    +RouterContract.Inputs.GoBack<TestRoutes>()
                }
                resultsIn {
                    assertEquals(
                        Destination.Match(
                            "/test/new",
                            TestRoutes.Create,
                        ),
                        latestState.backstack.currentDestinationOrNotFound
                    )
                }
            }
            scenario("clear backstack") {
                running {
                    +RouterContract.Inputs.GoToDestination<TestRoutes>("/test/new")
                    +RouterContract.Inputs.GoToDestination<TestRoutes>("/test/12345")
                    +RouterContract.Inputs.GoBack<TestRoutes>()
                    +RouterContract.Inputs.GoBack<TestRoutes>()
                }
                resultsIn {
                    assertEquals(
                        null,
                        latestState.backstack.currentDestinationOrNotFound
                    )
                    assertEquals(
                        listOf(
                            RouterContract.Events.BackstackChanged(
                                listOf(
                                    destinationForCreate,
                                ),
                            ),
                            RouterContract.Events.BackstackChanged(
                                listOf(
                                    destinationForCreate,
                                    destinationForDetails,
                                ),
                            ),
                            RouterContract.Events.BackstackChanged(
                                listOf(
                                    destinationForCreate,
                                ),
                            ),
                            RouterContract.Events.BackstackEmptied(),
                        ),
                        this.events,
                    )
                }
            }
            scenario("routing with query params") {
                running {
                    +RouterContract.Inputs.GoToDestination<TestRoutes>("/test")
                    +RouterContract.Inputs.GoToDestination<TestRoutes>("/test?pageSize=25&page=2")
                    +RouterContract.Inputs.GoToDestination<TestRoutes>("/test?sort=asc&pageSize=25&page=2")
                }
                resultsIn {
                    assertEquals(
                        destinationForListWithSort,
                        latestState.backstack.currentDestinationOrNotFound
                    )
                    assertEquals(
                        listOf(
                            RouterContract.Events.BackstackChanged<TestRoutes>(
                                listOf(
                                    Destination.Mismatch("/test"),
                                ),
                            ),
                            RouterContract.Events.BackstackChanged<TestRoutes>(
                                listOf(
                                    destinationForList,
                                ),
                            ),
                            RouterContract.Events.BackstackChanged<TestRoutes>(
                                listOf(
                                    destinationForList,
                                    destinationForListWithSort,
                                ),
                            ),
                        ),
                        this.events,
                    )
                }
            }
        }
}
