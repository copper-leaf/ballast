package com.copperleaf.ballast.navigation.routing

/**
 * Represents an entry in the router's backstack. A destination is parsed from the URL sent to the router, and then
 * matched against the registered routes to attempt to find a route that is able to properly parse the destination URL '
 * into parameters.
 *
 * Routes are weighted, and then compared by weight against the dstination URL. The first route that is able to process
 * the route will be selected as the match, there is no guarantee that it will be the only matching route. If multiple
 * routes have the same weight, it is undefined which one will be selected. In this case, you may consider providing a
 * hardcoded weight to those routes to ensure one is selected over the other.
 */
public sealed interface Destination<T : Route> {

    /**
     * The original unmodified String URL sent to the Router.
     */
    public val originalDestinationUrl: String

    /**
     * The router was able to match the destination URL to a registered route. The [pathParameters] and
     * [queryParameters] are only those that were parsed by the route as parameters; values that were static or ignored
     * are not included in those maps.
     */
    public data class Match<T : Route>(
        override val originalDestinationUrl: String,
        val originalRoute: T,

        public override val pathParameters: Map<String, List<String>> = emptyMap(),
        public override val queryParameters: Map<String, List<String>> = emptyMap(),

        public val annotations: Set<RouteAnnotation> = emptySet(),
    ) : Destination<T>, Parameters, ParametersProvider {
        override fun toString(): String {
            return "'${originalDestinationUrl}'"
        }

        override val parameters: Parameters get() = this
    }

    /**
     * The router was not able to match the destination URL to a registered route. It will only ever be at the top of
     * the backstack, never in the middle, and will be removed automatically upon a subsequent navigation.
     */
    public data class Mismatch<T : Route>(
        override val originalDestinationUrl: String,
    ) : Destination<T> {
        override fun toString(): String {
            return "'${originalDestinationUrl}' (not found)"
        }
    }

    /**
     * A holder for parameters pulled from the destination URL.
     */
    public interface Parameters {
        public val pathParameters: Map<String, List<String>>
        public val queryParameters: Map<String, List<String>>
    }

    /**
     * Mark a class as one that provides [Parameters] lazily for use by delegate functions like [stringPath].
     */
    public interface ParametersProvider {
        public val parameters: Parameters
    }

    public data class Directions<T : Route>(
        val route: T,
        override val pathParameters: Map<String, List<String>> = emptyMap(),
        override val queryParameters: Map<String, List<String>> = emptyMap(),
    ) : Parameters
}
