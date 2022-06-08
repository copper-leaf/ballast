package com.copperleaf.ballast.navigation.routing

import com.copperleaf.ballast.BallastViewModelConfiguration
import com.copperleaf.ballast.core.BootstrapInterceptor
import com.copperleaf.ballast.core.FifoInputStrategy
import com.copperleaf.ballast.plusAssign
import io.ktor.http.encodeURLPath
import io.ktor.http.encodeURLQueryComponent

public val RouterContract.State.currentDestination: Destination?
    get() {
        return backstack
            .lastOrNull {
                it is Destination
            }
            as? Destination?
    }

public val RouterContract.State.currentDestinationOrNotFound: NavToken?
    get() {
        return backstack.lastOrNull {
            when (it) {
                is Destination -> true
                is MissingDestination -> true
                else -> false
            }
        }
    }

public fun Route.asStartDestination(): Destination {
    check(this.matcher.path.all { it is PathSegment.Static }) {
        "For a Route to be used as a Start Destination, it must be fully static " +
            "(no path parameters, wildcards, or tailcards)"
    }

    return this.matchDestinationOrThrow(originalRoute)
}

public fun Route.asStartDestinationString(): String {
    check(this.matcher.path.all { it is PathSegment.Static }) {
        "For a Route to be used as a Start Destination, it must be fully static " +
            "(no path parameters, wildcards, or tailcards)"
    }

    return this.originalRoute
}

public fun Route.asInitialBackstack(): List<Destination> {
    return listOf(this.asStartDestination())
}

public fun BallastViewModelConfiguration.Builder.withRouter(
    navGraph: NavGraph,
    initialRoute: Route = navGraph.routes.first(),
): BallastViewModelConfiguration.Builder =
    this
        .apply {
            this.inputStrategy = FifoInputStrategy()
            this.inputHandler = RouterInputHandler()
            this.initialState = RouterContract.State(navGraph = navGraph)
            this.name = "Router"

            this += BootstrapInterceptor {
                RouterContract.Inputs.GoToDestination(
                    initialRoute.asStartDestinationString()
                )
            }
        }

public fun Route.directions(
    pathParameters: Map<String, List<String>> = emptyMap(),
    queryParameters: Map<String, List<String>> = emptyMap(),
): String {
    // check that the provided path parameters exactly match what's needed in the route
    val extraKeys: Collection<String> = pathParameters.keys - this.matcher.path.mapNotNull { it.paramName }.toSet()

    check(extraKeys.isEmpty()) {
        "The following path parameter values could not be found in the directions to route '${this.originalRoute}': $extraKeys"
    }

    val formattedQueryString = if (queryParameters.isNotEmpty()) {
        val duplicatedQueryParams: List<Pair<String, String>> = queryParameters.entries.flatMap { (key, values) ->
            values.map { value -> key to value }
        }

        "?" + duplicatedQueryParams.joinToString(separator = "&") { (key, value) ->
            val encodedKey = key.trim().encodeURLQueryComponent()
            val encodedValue = value.trim().encodeURLQueryComponent()
            "$encodedKey=$encodedValue"
        }
    } else {
        ""
    }

    val formattedPath = this
        .matcher
        .path
        .mapNotNull {
            when (it) {
                is PathSegment.Static -> listOf(it.text)
                is PathSegment.Parameter -> {
                    val pathValue = if (!it.optional) {
                        checkNotNull(pathParameters[it.name]) {
                            "Non-optional path parameter '${it.name}' must be provided in destination to route '${this.originalRoute}'"
                        }
                    } else {
                        pathParameters[it.name]
                    }

                    pathValue?.single()?.encodeURLPath()?.let { listOf(it) }
                }
                is PathSegment.Wildcard -> {
                    error("Cannot create directions for wildcard path segment, consider switching to a named parameter instead in route '${this.originalRoute}'")
                }
                is PathSegment.Tailcard -> {
                    checkNotNull(it.name) {
                        "Cannot create directions for unnamed tailcard path segment, consider switching to a named tailcard instead in route '${this.originalRoute}'"
                    }
                    pathParameters[it.name]
                }
            }
        }
        .flatten()
        .map { it.trim() }
        .joinToString(separator = "/", prefix = "/")

    return "$formattedPath$formattedQueryString"
}
