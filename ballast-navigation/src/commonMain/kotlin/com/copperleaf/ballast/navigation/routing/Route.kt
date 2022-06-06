package com.copperleaf.ballast.navigation.routing

public data class Route internal constructor(
    val originalRoute: String,
    val matcher: RouteMatcher,
) {
    public companion object {
        /**
         * Create a Route and compute its weight automatically
         */
        public operator fun invoke(
            originalRoute: String,
        ): Route {
            return Route(
                originalRoute = originalRoute,
                matcher = originalRoute.createMatcher()
            )
        }

        /**
         * Create a Route with a hardcoded weight
         */
        public operator fun invoke(
            originalRoute: String,
            weight: Double,
        ): Route {
            return Route(
                originalRoute = originalRoute,
                matcher = originalRoute.createMatcher { weight }
            )
        }
    }
}
