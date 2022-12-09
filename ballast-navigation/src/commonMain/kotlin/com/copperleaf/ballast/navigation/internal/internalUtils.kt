package com.copperleaf.ballast.navigation.internal

import com.copperleaf.ballast.navigation.routing.Destination
import com.copperleaf.ballast.navigation.routing.PathSegment
import com.copperleaf.ballast.navigation.routing.QueryParameter
import com.copperleaf.ballast.navigation.routing.Route
import io.ktor.http.encodeURLPathPart
import io.ktor.http.encodeURLQueryComponent

// Directions
// ---------------------------------------------------------------------------------------------------------------------

internal fun Route.directionsInternal(
    parameters: Destination.Parameters,
): String {
    val formattedPath = directionsForPath(parameters.pathParameters)
    val formattedQueryString = directionsForQuery(parameters.queryParameters)

    if (formattedPath.isFailure || formattedQueryString.isFailure) {
        error(directionsErrorMessage(parameters, formattedPath, formattedQueryString))
    }

    return "${formattedPath.getOrThrow()}${formattedQueryString.getOrThrow()}"
}

internal fun Route.directionsForPath(
    pathParameters: Map<String, List<String>>,
): Result<String> {
    val pathDirections: Pair<Map<String, List<String>>, List<PathSegment.Directions>> = matcher
        .path
        .fold(pathParameters to emptyList()) { (unusedParams, acc), next ->
            val directionsForSegment = next.directions(unusedParams)

            val remainingParams: Map<String, List<String>> = directionsForSegment
                .consumedParameterName
                ?.let { unusedParams - it }
                ?: unusedParams

            remainingParams to (acc + directionsForSegment)
        }

    val allResults = if (pathDirections.first.isNotEmpty()) {
        pathDirections.second + PathSegment.Directions(error = "The following path parameters could not be matched: ${pathDirections.first.keys}")
    } else {
        pathDirections.second
    }
    val (successResults, failureResults) = allResults.partition { it.error == null }

    return if (failureResults.isNotEmpty()) {
        Result.failure(
            RuntimeException(
                failureResults.joinToString(separator = "\n") { "  - ${it.error}" }
            )
        )
    } else {
        Result.success(
            successResults
                .flatMap { it.toAppend }
                .joinToString(separator = "/", prefix = "/") { it.encodeURLPathPart() }
        )
    }
}

internal fun Route.directionsForQuery(
    queryParameters: Map<String, List<String>>,
): Result<String> {
    val queryDirections: Pair<Map<String, List<String>>, List<QueryParameter.Directions>> = matcher
        .query
        .fold(queryParameters to emptyList()) { (unusedParams, acc), next ->
            val directionsForSegment = next.directions(unusedParams)

            val remainingParams: Map<String, List<String>> = directionsForSegment
                .consumedParameterNames
                .let { unusedParams - it }

            remainingParams to (acc + directionsForSegment)
        }

    val allResults = if (queryDirections.first.isNotEmpty()) {
        queryDirections.second + QueryParameter.Directions(
            error = "The following query parameters could not be matched: ${queryDirections.first.keys}"
        )
    } else {
        queryDirections.second
    }
    val (successResults, failureResults) = allResults
        .filter { it.toAppend.isNotEmpty() || it.consumedParameterNames.isNotEmpty() || it.error != null }
        .partition { it.error == null }

    return if (failureResults.isNotEmpty()) {
        Result.failure(
            RuntimeException(
                failureResults.joinToString(separator = "\n") { "  - ${it.error}" }
            )
        )
    } else {
        Result.success(
            if (successResults.isEmpty()) {
                ""
            } else {
                successResults
                    .flatMap {
                        it.toAppend.flatMap { (key, values) ->
                            values.map { value -> key to value }
                        }
                    }
                    .joinToString(separator = "&", prefix = "?") { (key, value) ->
                        val encodedKey = key.encodeURLQueryComponent(encodeFull = true, spaceToPlus = true)
                        val encodedValue = value.encodeURLQueryComponent(encodeFull = true, spaceToPlus = true)
                        "$encodedKey=$encodedValue"
                    }
            }
        )
    }
}

internal fun Route.directionsErrorMessage(
    parameters: Destination.Parameters,
    formattedPath: Result<String>,
    formattedQueryString: Result<String>,
): String {
    return buildString {
        appendLine("Error creating directions to route '${this@directionsErrorMessage}'")
        appendLine()

        if (parameters.pathParameters.isNotEmpty()) {
            appendLine("Path Parameters:")
            parameters.pathParameters.entries.forEach { (key, values) ->
                appendLine("  - [$key] (${values.size} ${if (values.size == 1) "value" else "values"})")
            }
        }
        if (parameters.queryParameters.isNotEmpty()) {
            appendLine("Query Parameters:")
            parameters.queryParameters.entries.forEach { (key, values) ->
                appendLine("  - [$key] (${values.size} ${if (values.size == 1) "value" else "values"})")
            }
        }

        appendLine("Errors:")
        if (formattedPath.isFailure) {
            append(formattedPath.exceptionOrNull()?.message)
        }
        if (formattedQueryString.isFailure) {
            append(formattedQueryString.exceptionOrNull()?.message)
        }
    }
}
