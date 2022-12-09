package com.copperleaf.ballast.navigation.internal

import com.copperleaf.ballast.navigation.routing.PathSegment
import com.copperleaf.ballast.navigation.routing.QueryParameter
import com.copperleaf.ballast.navigation.routing.RouteMatcher
import com.copperleaf.kudzu.node.mapped.ValueNode
import com.copperleaf.kudzu.parser.Parser
import com.copperleaf.kudzu.parser.ParserContext
import com.copperleaf.kudzu.parser.ParserResult
import com.copperleaf.kudzu.parser.chars.CharInParser
import com.copperleaf.kudzu.parser.mapped.MappedParser
import com.copperleaf.kudzu.parser.maybe.MaybeParser
import com.copperleaf.kudzu.parser.sequence.SequenceParser
import kotlin.math.pow

internal object RouteParser {

    private val routeParser: Parser<ValueNode<Pair<List<PathSegment>, List<QueryParameter>>>> = MappedParser(
        SequenceParser(
            PathParser.pathParser,
            MaybeParser(
                SequenceParser(
                    CharInParser('?'),
                    QueryStringParser.queryStringParser,
                )
            ),
        )
    ) { (_, path, query) ->
        val pathParameters: List<PathSegment> = path.value
        val queryParameters: List<QueryParameter> = query.node?.node2?.value ?: emptyList()

        pathParameters to queryParameters
    }

    internal fun parseRoute(
        routeFormat: String,
        computeWeight: (List<PathSegment>, List<QueryParameter>) -> Double,
    ): RouteMatcher {
        val parseResult = routeParser.parse(ParserContext.fromString(routeFormat))
        validateCorrectFormat(routeFormat, parseResult)

        val (pathParameters, queryParameters) = parseResult.first.value
        validatePathParameters(pathParameters)
        validateQueryParameters(queryParameters)
        validateAggregatedParameters(pathParameters, queryParameters)

        return RouteMatcherImpl(
            routeFormat = routeFormat,
            path = pathParameters,
            query = queryParameters,
            weight = computeWeight(pathParameters, queryParameters),
        )
    }

// Helpers
// ---------------------------------------------------------------------------------------------------------------------

    internal fun computeWeight(pathSegments: List<PathSegment>, queryParameters: List<QueryParameter>): Double {

        // we require 2 more query parameters than the number of path segments for query parameters to be considered more
        // specific than the path
        val pathPowerModifier = queryParameters.size + 1

        // path has a greater weight than the query, so raise its power by the number of query paramers to ensure it
        // always matches paths before query parameters
        val pathWeight = pathSegments.reversed().foldRightIndexed(0.0) { index, next, acc ->
            acc + (next.weight * (10.0.pow(index + pathPowerModifier)))
        }

        val queryWeight = queryParameters.reversed().foldRightIndexed(0.0) { index, next, acc ->
            acc + (next.weight * (10.0.pow(index)))
        }

        return pathWeight + queryWeight
    }

    private fun validateCorrectFormat(route: String, parserResult: ParserResult<*>) {
        check(parserResult.second.isEmpty()) {
            "'$route' is not a valid route format"
        }
    }

    private fun validatePathParameters(pathSegments: List<PathSegment>) {
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
    }

    private fun validateQueryParameters(queryParameters: List<QueryParameter>) {
        if (queryParameters.any { it.mustBeAtEnd }) {
            // if we have any remainder parameters, it must be at the end of the query string and be unique

            if (queryParameters.count { it.mustBeAtEnd } > 1) {
                error("you can only have one remainder query parameter")
            }
            if (!queryParameters.last().mustBeAtEnd) {
                error("remainder query parameter must be at the end")
            }
        }
    }

    private fun validateAggregatedParameters(pathSegments: List<PathSegment>, queryParameters: List<QueryParameter>) {
        val paramNames = pathSegments.mapNotNull { it.paramName } + queryParameters.mapNotNull { it.paramName }
        val distinctParamNames = paramNames.distinct()
        if (distinctParamNames.size != paramNames.size) {
            error("parameter names must be unique")
        }
    }
}
