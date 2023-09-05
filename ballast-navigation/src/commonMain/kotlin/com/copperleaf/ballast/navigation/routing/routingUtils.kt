@file:Suppress("NOTHING_TO_INLINE")

package com.copperleaf.ballast.navigation.routing

import com.copperleaf.ballast.navigation.internal.EnumRoutingTable
import com.copperleaf.ballast.navigation.internal.directionsInternal
import kotlin.properties.PropertyDelegateProvider

public typealias Backstack<T> = List<Destination<T>>

// Use router
// ---------------------------------------------------------------------------------------------------------------------

/**
 * Determine whether this Route is static. A static Route is one where `Route.directions()` will produce a URL which
 * matches itself when no path or query parameters are provided to the [Route.directions] function.
 */
public fun Route.isStatic(): Boolean {
    return matcher.path.all { it.isStatic } && matcher.query.all { it.isStatic }
}

/**
 * Start building a destination with directions from [this] [Route].
 */
public fun <T : Route> T.directions(): Destination.Directions<T> {
    return Destination.Directions(this)
}

/**
 * Convert the directions from a route and its parameters into a final destination URL.
 */
public fun <T : Route> Destination.Directions<T>.build(): String {
    return route.directionsInternal(this)
}

/**
 * Set the value of the path parameters map, overwriting any previous entries. The named values in the Route's path
 * format will be used to infer the proper values based on their index in the [pathParameters] array, so you can more
 * easily provide the values for parameters and tailcards without needing to explicitly refer to the parameter names
 * for them every time.
 *
 * @return a new Builder instance with the new path values
 */
public fun <T : Route> Destination.Directions<T>.path(
    vararg pathParameters: String,
): Destination.Directions<T> {
    return this.copy(
        pathParameters = buildMap {
            var i = 0
            route.matcher.path.forEach { segment ->
                when (segment) {
                    is PathSegment.Static -> {
                        // nothing to add
                    }

                    is PathSegment.Parameter -> {
                        put(segment.name, listOfNotNull(pathParameters.getOrNull(i)))
                        i++
                    }

                    is PathSegment.Wildcard -> {
                        // nothing to add
                    }

                    is PathSegment.Tailcard -> {
                        if (segment.name != null) {
                            put(segment.name, pathParameters.toList().subList(i, pathParameters.size))
                        }
                        i = pathParameters.lastIndex
                    }
                }
            }
        },
    )
}

/**
 * Add the values in [pathMap] to the [Destination.Directions.pathParameters] of this builder.
 *
 * @return a new Builder instance with the new path values
 */
public fun <T : Route> Destination.Directions<T>.pathParameters(
    pathMap: Map<String, List<String>>
): Destination.Directions<T> {
    return this.copy(
        pathParameters = this.pathParameters + pathMap,
    )
}

/**
 * Add a single path parameter to the builder at the given [key] with multiple [values].
 *
 * @return a new Builder instance with the new path value
 */
public fun <T : Route> Destination.Directions<T>.pathParameter(
    key: String,
    vararg values: String,
): Destination.Directions<T> {
    return this.copy(
        pathParameters = this.pathParameters + (key to values.toList()),
    )
}

/**
 * Add a single path parameter to the builder at the given [key] with multiple [values].
 *
 * @return a new Builder instance with the new path value
 */
public fun <T : Route> Destination.Directions<T>.pathParameter(
    key: String,
    values: Iterable<String>,
): Destination.Directions<T> {
    return this.copy(
        pathParameters = this.pathParameters + (key to values.toList()),
    )
}

/**
 * Add the values in [queryMap] to the [Destination.Directions.queryParameters] of this builder.
 *
 * @return a new Builder instance with the new query values
 */
public fun <T : Route> Destination.Directions<T>.queryParameters(
    queryMap: Map<String, List<String>>
): Destination.Directions<T> {
    return this.copy(
        queryParameters = this.queryParameters + queryMap,
    )
}

/**
 * Add a single query parameter to the builder at the given [key] with multiple [values].
 *
 * @return a new Builder instance which includes the new query value
 */
public fun <T : Route> Destination.Directions<T>.queryParameter(
    key: String,
    vararg values: String,
): Destination.Directions<T> {
    return this.copy(
        queryParameters = this.queryParameters + (key to values.toList()),
    )
}

/**
 * Add a single query parameter to the builder at the given [key] with multiple [values].
 *
 * @return a new Builder instance which includes the new query value
 */
public fun <T : Route> Destination.Directions<T>.queryParameter(
    key: String,
    values: Iterable<String>,
): Destination.Directions<T> {
    return this.copy(
        queryParameters = this.queryParameters + (key to values.toList()),
    )
}

/**
 * Get the topmost matching destination from the backstack. If there are any mismatched destinations, those will be
 * skipped so that you can continue displaying the topmost matching destination. Returns null if the backstack is empty
 * or the only destination in the backstack is a mismatch.
 */
public val <T : Route> Backstack<T>.currentDestinationOrNull: Destination.Match<T>?
    get() = lastOfInstanceOrNull<Destination.Match<T>>()

/**
 * Get the topmost Route from the backstack, whether it is a match or mismatch. This allows you to explicitly
 * display the mismatched state from the backstack, rather than effectively ignoring it.
 */
public val <T : Route> Backstack<T>.currentRouteOrNull: T?
    get() = currentDestinationOrNull?.originalRoute

/**
 * Get the topmost matching destination from the backstack. If there are any mismatched destinations, those will be
 * skipped so that you can continue displaying the topmost matching destination. Throws an exception if the backstack is
 * empty or the only destination in the backstack is a mismatch.
 */
public val <T : Route> Backstack<T>.currentDestinationOrThrow: Destination.Match<T>
    get() = lastOfInstance<Destination.Match<T>>()

/**
 * Get the topmost matching Route from the backstack. If there are any mismatched destinations, those will be
 * skipped so that you can continue displaying the topmost matching destination. Throws an exception if the backstack is
 * empty or the only destination in the backstack is a mismatch.
 */
public val <T : Route> Backstack<T>.currentRouteOrThrow: T
    get() = currentDestinationOrThrow.originalRoute

/**
 * Get the topmost destination from the backstack, whether it is a match or mismatch. This allows you to explicitly
 * display the mismatched state from the backstack, rather than effectively ignoring it.
 */
public val <T : Route> Backstack<T>.currentDestinationOrNotFound: Destination<T>?
    get() = lastOrNull()

/**
 * Get all destinations from the backstack that have the given [annotation]. The matching destinations will only be
 * counted until the first entry without the given annotation is encountered, even if there are more after that deeper
 * in the backstack. This is useful for situations like displaying all [Floating] destinations that are above a
 * non-floating one.
 */
public fun <T : Route> Backstack<T>.getTopDestinationsWithAnnotation(annotation: RouteAnnotation): List<Destination.Match<T>> =
    takeLastWhile { it is Destination.Match && annotation in it.annotations }
        .map { it as Destination.Match }

/**
 * Get the first destination from the backstack that does not have the given [annotation]. This is not necessarily the
 * top destination in the backstack. This is useful for situations like displaying the main content underneath
 * [Floating] destinations, so that the main content is still visible under the scrim of the floating window.
 */
public fun <T : Route> RouterContract.State<T>.getTopDestinationWithoutAnnotation(annotation: RouteAnnotation): Destination.Match<T>? =
    backstack
        .lastOrNull { it is Destination.Match<T> && annotation !in it.annotations } as? Destination.Match<T>

public inline fun <T : Route> Backstack<T>.renderCurrentDestination(
    route: Destination.Match<T>.(T) -> Unit,
    notFound: (String) -> Unit,
) {
    when (val currentDestination = this.currentDestinationOrNotFound) {
        is Destination.Match -> {
            route(currentDestination, currentDestination.originalRoute)
        }

        is Destination.Mismatch -> {
            notFound(currentDestination.originalDestinationUrl)
        }

        null -> {
        }
    }
}

public inline fun <T : Route, U : Any> Backstack<T>.mapCurrentDestination(
    route: Destination.Match<T>.(T) -> U?,
    notFound: (String) -> U,
): U? {
    return when (val currentDestination = this.currentDestinationOrNotFound) {
        is Destination.Match -> {
            route(currentDestination, currentDestination.originalRoute)
        }

        is Destination.Mismatch -> {
            notFound(currentDestination.originalDestinationUrl)
        }

        null -> {
            null
        }
    }
}

// Other helpers
// ---------------------------------------------------------------------------------------------------------------------

/**
 * Returns the last element matching whose type is [T], or `null` if no such element was found.
 */
private inline fun <reified T> List<*>.lastOfInstanceOrNull(): T? {
    val iterator = this.listIterator(size)
    while (iterator.hasPrevious()) {
        val element = iterator.previous()
        if (element is T) return element
    }
    return null
}

/**
 * Returns the last element matching whose type is [T].
 *
 * @throws NoSuchElementException if no such element is found.
 */
private inline fun <reified T> List<*>.lastOfInstance(): T {
    val iterator = this.listIterator(size)
    while (iterator.hasPrevious()) {
        val element = iterator.previous()
        if (element is T) return element
    }
    throw NoSuchElementException("List contains no element matching the predicate.")
}

// Delegate Base
// ---------------------------------------------------------------------------------------------------------------------

public typealias LazyProvider<T> = PropertyDelegateProvider<Any?, Lazy<T>>

private fun <T> provideLazy(
    compute: (propertyName: String) -> T
): LazyProvider<T> = PropertyDelegateProvider { _, property ->
    lazy<T> {
        compute(property.name)
    }
}

public fun <T : Any> Destination.ParametersProvider.provideLazyPath(
    parameterName: String?,
    compute: (value: String) -> T
): LazyProvider<T> = provideLazy { propertyName ->
    parameters
        .pathParameters[parameterName ?: propertyName]!!
        .single()
        .let { compute(it) }
}

public fun <T : Any> Destination.ParametersProvider.provideLazyQuery(
    parameterName: String?,
    compute: (value: String) -> T
): LazyProvider<T> = provideLazy { propertyName ->
    parameters
        .queryParameters[parameterName ?: propertyName]!!
        .single()
        .let { compute(it) }
}

public fun <T : Any> Destination.ParametersProvider.provideOptionalLazyPath(
    parameterName: String?,
    compute: (value: String) -> T?
): LazyProvider<T?> = provideLazy { propertyName ->
    parameters
        .pathParameters[parameterName ?: propertyName]
        ?.singleOrNull()
        ?.let { compute(it) }
}

public fun <T : Any> Destination.ParametersProvider.provideOptionalLazyQuery(
    parameterName: String?,
    compute: (value: String) -> T?
): LazyProvider<T?> = provideLazy { propertyName ->
    parameters
        .queryParameters[parameterName ?: propertyName]
        ?.singleOrNull()
        ?.let { compute(it) }
}

// Path Parameter delegates
// ---------------------------------------------------------------------------------------------------------------------

public inline fun Destination.ParametersProvider.stringPath(
    parameterName: String? = null
): LazyProvider<String> = provideLazyPath(parameterName) { it }

public inline fun Destination.ParametersProvider.optionalStringPath(
    parameterName: String? = null
): LazyProvider<String?> = provideOptionalLazyPath(parameterName) { it }

public inline fun Destination.ParametersProvider.intPath(
    parameterName: String? = null
): LazyProvider<Int> = provideLazyPath(parameterName) { it.toInt() }

public inline fun Destination.ParametersProvider.optionalIntPath(
    parameterName: String? = null
): LazyProvider<Int?> = provideOptionalLazyPath(parameterName) { it.toIntOrNull() }

public inline fun Destination.ParametersProvider.longPath(
    parameterName: String? = null
): LazyProvider<Long> = provideLazyPath(parameterName) { it.toLong() }

public inline fun Destination.ParametersProvider.optionalLongPath(
    parameterName: String? = null
): LazyProvider<Long?> = provideOptionalLazyPath(parameterName) { it.toLongOrNull() }

public inline fun Destination.ParametersProvider.floatPath(
    parameterName: String? = null
): LazyProvider<Float> = provideLazyPath(parameterName) { it.toFloat() }

public inline fun Destination.ParametersProvider.optionalFloatPath(
    parameterName: String? = null
): LazyProvider<Float?> = provideOptionalLazyPath(parameterName) { it.toFloatOrNull() }

public inline fun Destination.ParametersProvider.doublePath(
    parameterName: String? = null
): LazyProvider<Double> = provideLazyPath(parameterName) { it.toDouble() }

public inline fun Destination.ParametersProvider.optionalDoublePath(
    parameterName: String? = null
): LazyProvider<Double?> = provideOptionalLazyPath(parameterName) { it.toDoubleOrNull() }

public inline fun Destination.ParametersProvider.booleanPath(
    parameterName: String? = null
): LazyProvider<Boolean> = provideLazyPath(parameterName) { it.toBooleanStrict() }

public inline fun Destination.ParametersProvider.optionalBooleanPath(
    parameterName: String? = null
): LazyProvider<Boolean?> = provideOptionalLazyPath(parameterName) { it.toBooleanStrictOrNull() }

public inline fun <T : Enum<T>> Destination.ParametersProvider.enumPath(
    crossinline valueOf: (String) -> T,
    parameterName: String? = null,
): LazyProvider<T> = provideLazyPath(parameterName) { valueOf(it) }

public inline fun <T : Enum<T>> Destination.ParametersProvider.optionalEnumPath(
    crossinline valueOf: (String) -> T,
    parameterName: String? = null
): LazyProvider<T?> = provideOptionalLazyPath(parameterName) { runCatching { valueOf(it) }.getOrNull() }

// Query Parameter delegates
// ---------------------------------------------------------------------------------------------------------------------

public inline fun Destination.ParametersProvider.stringQuery(
    parameterName: String? = null
): LazyProvider<String> = provideLazyQuery(parameterName) { it }

public inline fun Destination.ParametersProvider.optionalStringQuery(
    parameterName: String? = null
): LazyProvider<String?> = provideOptionalLazyQuery(parameterName) { it }

public inline fun Destination.ParametersProvider.intQuery(
    parameterName: String? = null
): LazyProvider<Int> = provideLazyQuery(parameterName) { it.toInt() }

public inline fun Destination.ParametersProvider.optionalIntQuery(
    parameterName: String? = null
): LazyProvider<Int?> = provideOptionalLazyQuery(parameterName) { it.toIntOrNull() }

public inline fun Destination.ParametersProvider.longQuery(
    parameterName: String? = null
): LazyProvider<Long> = provideLazyQuery(parameterName) { it.toLong() }

public inline fun Destination.ParametersProvider.optionalLongQuery(
    parameterName: String? = null
): LazyProvider<Long?> = provideOptionalLazyQuery(parameterName) { it.toLongOrNull() }

public inline fun Destination.ParametersProvider.floatQuery(
    parameterName: String? = null
): LazyProvider<Float> = provideLazyQuery(parameterName) { it.toFloat() }

public inline fun Destination.ParametersProvider.optionalFloatQuery(
    parameterName: String? = null
): LazyProvider<Float?> = provideOptionalLazyQuery(parameterName) { it.toFloatOrNull() }

public inline fun Destination.ParametersProvider.doubleQuery(
    parameterName: String? = null
): LazyProvider<Double> = provideLazyQuery(parameterName) { it.toDouble() }

public inline fun Destination.ParametersProvider.optionalDoubleQuery(
    parameterName: String? = null
): LazyProvider<Double?> = provideOptionalLazyQuery(parameterName) { it.toDoubleOrNull() }

public inline fun Destination.ParametersProvider.booleanQuery(
    parameterName: String? = null
): LazyProvider<Boolean> = provideLazyQuery(parameterName) { it.toBooleanStrict() }

public inline fun Destination.ParametersProvider.optionalBooleanQuery(
    parameterName: String? = null
): LazyProvider<Boolean?> = provideOptionalLazyQuery(parameterName) { it.toBooleanStrictOrNull() }

public inline fun <T : Enum<T>> Destination.ParametersProvider.enumQuery(
    crossinline valueOf: (String) -> T,
    parameterName: String? = null,
): LazyProvider<T> = provideLazyQuery(parameterName) { valueOf(it) }

public inline fun <T : Enum<T>> Destination.ParametersProvider.optionalEnumQuery(
    crossinline valueOf: (String) -> T,
    parameterName: String? = null
): LazyProvider<T?> = provideOptionalLazyQuery(parameterName) { runCatching { valueOf(it) }.getOrNull() }

// Backstack Navigator helpers
// ---------------------------------------------------------------------------------------------------------------------

/**
 * Attempt to navigate forward to the route matching [destinationUrl]. If no matching route can be found,
 * [Destination.Mismatch] will be pushed onto the backstack instead.
 */
public fun <T : Route> BackstackNavigator<T>.addToTop(
    destinationUrl: String,
    extraAnnotations: Set<RouteAnnotation>,
) {
    return if (destinationUrl == backstack.currentDestinationOrNull?.originalDestinationUrl) {
        // same as top destination, ignore it
    } else {
        updateBackstack {
            val matchedDestination = matchDestination(destinationUrl, extraAnnotations)
            it.dropLastWhile { it is Destination.Mismatch } + matchedDestination
        }
    }
}

/**
 * Navigate backwards 1 location in the backstack.
 */
public fun <T : Route> BackstackNavigator<T>.goBack(steps: Int) {
    if (backstack.isEmpty()) {
        // error, backstack was empty. Just ignore it
    } else {
        updateBackstack {
            it.dropLast(steps)
        }
    }
}

/**
 * Navigate backward in the backstack, removing all destinations for which the predicate [shouldPop] returns true.
 */
public fun <T : Route> BackstackNavigator<T>.popWith(
    shouldPop: (Destination.Match<T>) -> Boolean,
) {
    if (backstack.isEmpty()) {
        // error, backstack was empty. Just ignore it
    } else {
        updateBackstack {
            it.dropLastWhile { destination ->
                when (destination) {
                    is Destination.Match<T> -> {
                        shouldPop(destination)
                    }

                    is Destination.Mismatch<T> -> {
                        // drop the mismatches, regardless
                        true
                    }
                }
            }
        }
    }
}

/**
 * Navigate backward in the backstack, removing all destinations that contain the given [annotation].
 */
public fun <T : Route> BackstackNavigator<T>.popAllWithAnnotation(
    annotation: RouteAnnotation,
) {
    popWith {
        annotation in it.annotations
    }
}

/**
 * Navigate backward in the backstack, removing all destinations for which the predicate [shouldPop] returns true.
 */
public fun <T : Route> BackstackNavigator<T>.popUntil(
    inclusive: Boolean,
    shouldPop: (Destination.Match<T>) -> Boolean,
) {
    if (backstack.isEmpty()) {
        // error, backstack was empty. Just ignore it
    } else {
        updateBackstack { backstack ->
            val index = backstack.indexOfLast { destination ->
                when (destination) {
                    is Destination.Match<T> -> {
                        shouldPop(destination)
                    }

                    is Destination.Mismatch<T> -> {
                        // drop the mismatches, regardless
                        false
                    }
                }
            }

            if (index == -1) {
                // error, no matching routes in the backstack. return an empty backstack.
                emptyList()
            } else if (inclusive) {
                backstack.subList(0, index)
            } else {
                backstack.subList(0, index + 1)
            }
        }
    }
}

/**
 * Navigate backward in the backstack, removing all destinations that contain the given [annotation].
 */
public fun <T : Route> BackstackNavigator<T>.popUntilRoute(
    inclusive: Boolean,
    route: T,
) {
    popUntil(inclusive) {
        it.originalRoute == route
    }
}


/**
 * Navigate backward in the backstack, removing all destinations that contain the given [annotation].
 */
public fun <T : Route> BackstackNavigator<T>.popUntilAnnotation(
    inclusive: Boolean,
    annotation: RouteAnnotation,
) {
    popUntil(inclusive) {
        annotation in it.annotations
    }
}

// Additional matcher methods
// ---------------------------------------------------------------------------------------------------------------------

public fun <T : Route> RouteMatcher.matchDestinationOrThrow(
    originalRoute: T,
    unmatchedDestination: UnmatchedDestination,
): Destination.Match<T> {
    val match = match(originalRoute, unmatchedDestination)
    return when (match) {
        is RouteMatcher.MatchResult.NoMatch<T> -> {
            error(
                "Destination '${unmatchedDestination.originalDestinationUrl}' does not match Route '$originalRoute'" +
                        ": Path mismatch"
            )
        }

        is RouteMatcher.MatchResult.PartialMatch<T> -> {
            error(
                "Destination '${unmatchedDestination.originalDestinationUrl}' does not match Route '$originalRoute'" +
                        ": Query string mismatch"
            )
        }

        is RouteMatcher.MatchResult.CompleteMatch<T> -> {
            unmatchedDestination.asMatchedDestination(match)
        }
    }
}

public fun <T : Route> RouteMatcher.matchDestinationOrNull(
    originalRoute: T,
    unmatchedDestination: UnmatchedDestination
): Destination.Match<T>? {
    return when (val match = match(originalRoute, unmatchedDestination)) {
        is RouteMatcher.MatchResult.NoMatch -> null
        is RouteMatcher.MatchResult.PartialMatch -> null
        is RouteMatcher.MatchResult.CompleteMatch -> unmatchedDestination.asMatchedDestination(match)
    }
}

public fun <T : Route> RouteMatcher.matchDestination(
    originalRoute: T,
    unmatchedDestination: UnmatchedDestination,
): Destination<T> {
    return when (val match = match(originalRoute, unmatchedDestination)) {
        is RouteMatcher.MatchResult.NoMatch -> unmatchedDestination.asMismatchedDestination()
        is RouteMatcher.MatchResult.PartialMatch -> unmatchedDestination.asMismatchedDestination()
        is RouteMatcher.MatchResult.CompleteMatch -> unmatchedDestination.asMatchedDestination(match)
    }
}

public fun <T : Route> UnmatchedDestination.asMatchedDestination(
    match: RouteMatcher.MatchResult.CompleteMatch<T>,
): Destination.Match<T> {
    return Destination.Match(
        originalDestinationUrl = originalDestinationUrl,
        originalRoute = match.originalRoute,
        pathParameters = match.parsedPathParameters,
        queryParameters = match.parsedQueryParameters,
        annotations = (match.originalRoute.annotations + extraAnnotations).toSet()
    )
}

public fun <T : Route> UnmatchedDestination.asMismatchedDestination(): Destination.Mismatch<T> {
    return Destination.Mismatch(
        originalDestinationUrl = originalDestinationUrl,
    )
}

// RoutingTable Helpers
// ---------------------------------------------------------------------------------------------------------------------

public fun <T> RoutingTable.Companion.fromEnum(
    enumValues: Array<T>,
): RoutingTable<T> where T : Enum<T>, T : Route {
    check(enumValues.isNotEmpty()) { "RoutingTable enum values cannot be empty" }

    val routesSortedByWeight: List<T> = enumValues
        .sortedByDescending { it.matcher.weight }

    return EnumRoutingTable(
        routes = routesSortedByWeight,
    )
}
