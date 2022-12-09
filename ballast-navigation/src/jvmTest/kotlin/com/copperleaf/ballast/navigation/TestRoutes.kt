package com.copperleaf.ballast.navigation

import com.copperleaf.ballast.navigation.routing.Route
import com.copperleaf.ballast.navigation.routing.RouteAnnotation
import com.copperleaf.ballast.navigation.routing.RouteMatcher

enum class TestRoutes(
    routeFormat: String,
    override val annotations: Set<RouteAnnotation> = emptySet(),
) : Route {
    ListAll("/test?sort={?}&pageSize={!}&page={!}"),
    Create("/test/new"),
    Edit("/test/{testId}/edit"),
    Delete("/test/{testId}/delete"),
    Details("/test/{testId}");

    override val matcher: RouteMatcher = RouteMatcher.create(routeFormat)
}
