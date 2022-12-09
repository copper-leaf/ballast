package com.copperleaf.ballast.navigation.routing

public interface Route {

    /**
     * The result of parsing a string route format to something that can be matched against a destination URL. This
     * should typically be implemented with [RouteMatcher.create] and wrapped in `by lazy { }` since parsing can be an
     * expensive operation.
     *
     * The route syntax is similar to a standard URL path and query string, but you may use placeholders in path
     * segments and query parameters to match those values dynamically. See [PathSegment] and [QueryParameter] for
     * example of the syntax available.
     */
    public val matcher: RouteMatcher

    /**
     * Metadata used to help in implementing custom routing behavior. These annotations will be added to those provided
     * directly to the router when navigating to a destination.
     */
    public val annotations: Set<RouteAnnotation>

}
