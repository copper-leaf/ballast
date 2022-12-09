package com.copperleaf.ballast.navigation.routing

/**
 * A scope for modifying the router's backstack.
 *
 * Navigation requests and backstack changes are transactional; A single Input sent to the Router may make multiple
 * changes to the backstack, and all updates will be batched and set as the Router's new State only once.
 *
 * This doesn't always need to be used directly, many navigational patterns can be handled with the Inputs provided
 * out-of-the-box in `RouterContract.Inputs`. You may create your own subclass of `RouterContract.Inputs` to receive
 * this navigator and make a transactional update with more custom logic.
 */
public interface BackstackNavigator<T : Route> {

    /**
     * The current state of the Backstack within the transaction.
     */
    public val backstack: Backstack<T>

    /**
     * Update the current state of the Backstack within this transaction. These changes will not be applied to the
     * actual Router ViewModel's state until the entire transaction has completed successfully.
     */
    public fun updateBackstack(block: (Backstack<T>) -> Backstack<T>)

    /**
     * Parse [destinationUrl] and amtch it against the routes registered in the [RoutingTable]. This will return either
     * [Destination.Match] if a matching route is capable of handling this URL, or else [Destination.Mismatch] if not.
     * Either way, the resulting entry can be placed in the backstack with [updateBackstack].
     */
    public fun matchDestination(
        destinationUrl: String,
        extraAnnotations: Set<RouteAnnotation> = emptySet(),
    ): Destination<T>
}
