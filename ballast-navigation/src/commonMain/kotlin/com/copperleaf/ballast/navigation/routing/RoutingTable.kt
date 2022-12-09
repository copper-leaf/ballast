package com.copperleaf.ballast.navigation.routing

/**
 * A container holding the routes registered to the Router. Navigation requests will be matched against the routing
 * table and will use [findMatch] to determine the best match for a given URL (represented by [UnmatchedDestination]).
 *
 * The routes are typically defined with an `enum class` and the routing table is created with
 * [RoutingTable.Companion.fromEnum], but the [Route]s registered with the RoutingTable are an interface, leaving room
 * for you to define your own route classes, match routes dynamically, or do any other custom logic for route matching
 * that you may need.
 */
public interface RoutingTable<T : Route> {

    /**
     * Attempt to find a suitable matching [Route] for the given [unmatchedDestination]. If a matching Route could be
     * found, [Destination.Match] will be returned with the Route, along with the path and query parameters extracted
     * from the Destination URL according to what was specified in the Route. Otherwise, [Destination.Mismatch] will be
     * returned.
     *
     * Regardless of whether the result is a Match or Mismatch, it may be placed into the [Backstack]. UIs should
     * display [Destination.Mismatch] in the UI with a helpful "not found" message, similar to a 404 page in a website.
     */
    public fun findMatch(
        unmatchedDestination: UnmatchedDestination
    ): Destination<T>

    public companion object
}
