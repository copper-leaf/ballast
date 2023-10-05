package com.copperleaf.ballast.examples.navigation

import com.copperleaf.ballast.navigation.routing.Route
import com.copperleaf.ballast.navigation.routing.RouteAnnotation
import com.copperleaf.ballast.navigation.routing.RouteMatcher

enum class AppScreen(
    routeFormat: String,
    override val annotations: Set<RouteAnnotation> = emptySet(),
) : Route {
    Home("/app/home"),
    PostList("/app/posts?sort={?}"),
    PostDetails("/app/posts/{postId}"),
    ;

    override val matcher: RouteMatcher = RouteMatcher.create(routeFormat)
}
