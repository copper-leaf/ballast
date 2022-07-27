package com.copperleaf.ballast.navigation.routing

import io.ktor.http.Url
import kotlin.math.pow

internal fun List<String>.matchablePathSegments(): List<String> {
    return dropWhile { it.isBlank() }.dropLastWhile { it.isBlank() }
}

internal fun String.parseDestination(): UnmatchedDestination {
    val url = Url(this)

    return UnmatchedDestination(
        originalUrl = this,
        path = url.encodedPath,
        pathSegments = url.pathSegments.matchablePathSegments(),
        queryParameters = url.parameters.entries().associate { it.key to it.value }
    )
}

internal fun String.toPathSegment(): PathSegment {
    if (this.startsWith(":")) {
        val paramName = this.drop(1)
        return PathSegment.Parameter(paramName, false)
    } else if (this.startsWith("{") && this.endsWith("}")) {
        val paramName = this.drop(1).dropLast(1)
        if (!paramName.endsWith("...")) {
            check(paramName.isNotBlank()) { "Path segment Parameter type must provide a name" }
            if (paramName.endsWith("?")) {
                val optionalParamName = paramName.dropLast(1)
                return PathSegment.Parameter(optionalParamName, true)
            } else {
                return PathSegment.Parameter(paramName, false)
            }
        } else {
            val tailcardParamName = paramName.dropLast(3)
            if (tailcardParamName.isNotBlank()) {
                return PathSegment.Tailcard(tailcardParamName)
            } else {
                return PathSegment.Tailcard(null)
            }
        }
    } else if (this == "*") {
        return PathSegment.Wildcard
    } else {
        return PathSegment.Static(this)
    }
}

internal val PathSegment.mustBeAtEnd: Boolean
    get() = when (this) {
        is PathSegment.Static -> false
        is PathSegment.Parameter -> this.optional
        is PathSegment.Wildcard -> false
        is PathSegment.Tailcard -> true
    }

internal val PathSegment.paramName: String?
    get() = when (this) {
        is PathSegment.Static -> null
        is PathSegment.Parameter -> name
        is PathSegment.Wildcard -> null
        is PathSegment.Tailcard -> name
    }

internal val PathSegment.weight: Int
    get() = when (this) {
        is PathSegment.Static -> 1
        is PathSegment.Parameter -> if (optional) 4 else 2
        is PathSegment.Wildcard -> 3
        is PathSegment.Tailcard -> 5
    }

internal fun String.createMatcher(computeWeight: (List<PathSegment>) -> Double = { it.weight }): RouteMatcher {
    val pathSegments = this.split("/").matchablePathSegments().map {
        it.toPathSegment()
    }

    if (pathSegments.any { it.mustBeAtEnd }) {
        // if we have any optional parameters or tailcards, they must be at the end of the path and be the only one of
        // its kind

        if (pathSegments.count { it.mustBeAtEnd } > 1) {
            error("you can only have one optional parameter or tailcard, but not both")
        }
        if (!pathSegments.last().mustBeAtEnd) {
            error("optional parameters and tailcards must be at the end of the path")
        }
    }
    val paramNames = pathSegments.mapNotNull { it.paramName }
    val distinctParamNames = paramNames.distinct()
    if (distinctParamNames.size != paramNames.size) {
        error("parameter names must be unique")
    }

    return RouteMatcher(path = pathSegments, weight = computeWeight(pathSegments))
}

internal fun Route.matchDestinationOrThrow(unmatchedDestination: UnmatchedDestination): Destination {
    var i = 0
    val pathParameters = buildMap {
        matcher.path.forEach { currentPathSegment ->
            val numberOfConsumedSegments = currentPathSegment.matchDestinationPathSegments(
                unmatchedDestination.pathSegments,
                i
            )
            check(numberOfConsumedSegments >= 0)

            try {
                val pathSegmentValues = unmatchedDestination.pathSegments.subList(i, i + numberOfConsumedSegments)

                when (currentPathSegment) {
                    is PathSegment.Static -> {}
                    is PathSegment.Wildcard -> {}
                    is PathSegment.Parameter -> {
                        if (currentPathSegment.optional) {
                            if (pathSegmentValues.isNotEmpty()) {
                                this[currentPathSegment.name] = pathSegmentValues
                            }
                        } else {
                            this[currentPathSegment.name] = pathSegmentValues
                        }
                    }
                    is PathSegment.Tailcard -> {
                        if (currentPathSegment.name != null) {
                            this[currentPathSegment.name] = pathSegmentValues
                        }
                    }
                }
            } catch (t: Throwable) {

            }

            i += numberOfConsumedSegments
        }
    }

    check(i == (unmatchedDestination.pathSegments.lastIndex + 1)) {
        "Destination (${unmatchedDestination.path}) does not match Route ($originalRoute)"
    }

    return Destination(
        originalUrl = unmatchedDestination.originalUrl,
        originalRoute = this,
        path = unmatchedDestination.path,
        pathSegments = unmatchedDestination.pathSegments,
        pathParameters = pathParameters,
        queryParameters = unmatchedDestination.queryParameters,
    )
}

internal fun Route.matchDestinationOrNull(unmatchedDestination: UnmatchedDestination): Destination? {
    return runCatching {
        matchDestinationOrThrow(unmatchedDestination)
    }.getOrNull()
}

internal fun Route.matchDestinationOrThrow(destination: String): Destination {
    return this.matchDestinationOrThrow(destination.parseDestination())
}

internal fun NavGraph.findMatch(destination: String): Destination? {
    val unmatchedDestination: UnmatchedDestination = destination.parseDestination()

    return this
        .routes
        .firstNotNullOfOrNull { it.matchDestinationOrNull(unmatchedDestination) }
}

internal val List<PathSegment>.weight: Double
    get() {
        return this.reversed().foldRightIndexed(0.0) { index, next, acc ->

            acc + (next.weight * (10.0.pow(index)))
        }
    }
