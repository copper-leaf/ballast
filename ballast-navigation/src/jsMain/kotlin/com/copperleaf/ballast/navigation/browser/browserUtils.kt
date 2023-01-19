package com.copperleaf.ballast.navigation.browser

import com.copperleaf.ballast.BallastViewModelConfiguration
import com.copperleaf.ballast.core.BootstrapInterceptor
import com.copperleaf.ballast.navigation.routing.Route
import com.copperleaf.ballast.navigation.routing.RoutingTable
import com.copperleaf.ballast.navigation.vm.withRouter
import com.copperleaf.ballast.plusAssign

/**
 * Configure a ViewModel to be used as a Router. This router will be set as your application's main router,
 * synchronizing its state with the browser's address bar using the
 * [History API](https://developer.mozilla.org/en-US/docs/Web/API/History_API). [initialRoute] should be provided as a
 * fallback for when a page is requested without an initial destination set in the URL.
 *
 * If [initialRoute] is provided, it will be automatically set as the
 * initial route using a [BootstrapInterceptor]. You may wish to keep this value as `null` to load the initial route
 * from some other means.
 */
public fun <T : Route> BallastViewModelConfiguration.Builder.withBrowserHistoryRouter(
    routingTable: RoutingTable<T>,
    basePath: String? = null,
    initialRoute: T,
): BallastViewModelConfiguration.Builder {
    return this
        .withRouter(routingTable, initialRoute = null)
        .apply {
            this += BrowserHistoryNavigationInterceptor<T>(basePath, initialRoute)
        }
}

/**
 * Configure a ViewModel to be used as a Router. This router will be set as your application's main router,
 * synchronizing its state with the browser's address bar using the hash-based routing. [initialRoute] should be
 * provided as a fallback for when a page is requested without an initial destination set in the URL.
 */
public fun <T : Route> BallastViewModelConfiguration.Builder.withBrowserHashRouter(
    routingTable: RoutingTable<T>,
    initialRoute: T,
): BallastViewModelConfiguration.Builder {
    return this
        .withRouter(routingTable, initialRoute = null)
        .apply {
            this += BrowserHashNavigationInterceptor<T>(initialRoute)
        }
}
