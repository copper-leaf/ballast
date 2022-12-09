package com.copperleaf.ballast.navigation.routing

/**
 * A generic marker for additional information you may pass to your routes to manage the backstack to UI in additional
 * ways. Route Annotations may be set at the Route itself, which will add that annotation to each Destination matched
 * with it, or it may be provided to the router along with the destination. Annotations themselves are strictly metadata
 * to help you construct your desired routing behavior, and are never considered when matching destinations to routes.
 *
 * Route annotations, like standard Annotation classes, should be considered effectively constant. Do not use them to
 * pass additional information to the route that would be used within the route's content. Those values should be
 * and parsed from the destination URL instead.
 */
public interface RouteAnnotation

/**
 * A simple tag to denote sub-graphs within the backstack, so that you can more easily exit the entire subgraph by
 * popping off all top destinations with that tag.
 */
public data class Tag(val value: String) : RouteAnnotation

/**
 * Define a route as a "floating windows", so that they do not replace the main destination by are shown above it. You
 * would find the top destination that is not marked as floating to display as the main content, and then all floating
 * destinations later than that entry in the backstack would be displayed in a stack above it with a visible scrim to
 * show that they are all floating over each other and over the main content.
 */
public object Floating : RouteAnnotation
