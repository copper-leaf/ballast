package com.copperleaf.ballast.navigation.routing

public abstract class RoutingTable(private val prefix: String? = null) {

    private val _routes: MutableList<Route> = mutableListOf()
    public val routes: List<Route> get() = _routes.toList()

    private var _initialRoute: Route? = null
    public val initialRoute: Route get() = _initialRoute ?: _routes.first() ?: error("Cannot find an initial route")

    private fun registerRoute(
        route: Route,
        isInitialRoute: Boolean,
    ) {
        _routes.add(route)
        if (isInitialRoute) {
            if (_initialRoute != null) {
                error("cannot have more than 1 initial route")
            }

            _initialRoute = route
        }
    }

    protected fun route(
        originalRoute: String,
        isInitialRoute: Boolean = false,
    ): Route {
        val resolvedRoute = if (prefix != null) "$prefix$originalRoute" else originalRoute
        return Route(resolvedRoute).also { registerRoute(it, isInitialRoute) }
    }

    protected fun route(
        originalRoute: String,
        weight: Double,
        isInitialRoute: Boolean = false,
    ): Route {
        val resolvedRoute = if (prefix != null) "$prefix$originalRoute" else originalRoute
        return Route(resolvedRoute, weight).also { registerRoute(it, isInitialRoute) }
    }

    protected fun route(
        route: Route,
        isInitialRoute: Boolean = false,
    ): Route {
        return route.also { registerRoute(it, isInitialRoute) }
    }
}
