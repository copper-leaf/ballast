package com.copperleaf.ballast.navigation.routing

public data class NavGraph internal constructor(
    val routes: List<Route>,
) {
    public companion object {
        public operator fun invoke(
            allRoutes: List<Route>,
        ): NavGraph {
            val routesSortedByWeight: List<Route> = allRoutes.sortedBy { it.matcher.weight }

            return NavGraph(
                routes = routesSortedByWeight
            )
        }

        public operator fun invoke(
            vararg allRoutes: Route,
        ): NavGraph {
            val routesSortedByWeight: List<Route> = allRoutes.sortedBy { it.matcher.weight }

            return NavGraph(
                routes = routesSortedByWeight
            )
        }

        public operator fun invoke(
            routingTable: RoutingTable,
        ): NavGraph {
            val routesSortedByWeight: List<Route> = routingTable.routes.sortedBy { it.matcher.weight }

            return NavGraph(
                routes = routesSortedByWeight
            )
        }
    }
}
