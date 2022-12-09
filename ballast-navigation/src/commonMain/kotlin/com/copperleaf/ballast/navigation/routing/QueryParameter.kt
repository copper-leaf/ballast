package com.copperleaf.ballast.navigation.routing

/**
 * The types of Query Parameters which may be parsed from a Route URL format.
 */
public sealed interface QueryParameter {

    public val paramName: String?
    public val weight: Int
    public val mustBeAtEnd: Boolean
    public val isStatic: Boolean

    public fun matchInDestination(
        queryParameters: Map<String, List<String>>,
    ): MatchResult

    public fun directions(
        queryParameters: Map<String, List<String>>,
    ): Directions

    /**
     * /route?one=two         // require a single value at this name
     * /route?one=[two,three] // require an exact list of values at this name
     */
    public data class Static(val name: String, val values: List<String>) : QueryParameter {
        override val paramName: String = name
        override val weight: Int = 5
        override val mustBeAtEnd: Boolean = false
        override val isStatic: Boolean = true

        override fun matchInDestination(queryParameters: Map<String, List<String>>): MatchResult {
            val matchingStaticParameter = queryParameters[name]?.sorted()
            return if (matchingStaticParameter == values) {
                MatchResult.Match(name)
            } else {
                MatchResult.Mismatch
            }
        }

        override fun directions(queryParameters: Map<String, List<String>>): Directions {
            return Directions(
                toAppend = mapOf(name to values),
                consumedParameterNames = emptySet()
            )
        }
    }

    /**
     * /route?one={!}  // require exactly 1 value
     * /route?one={[!]} // require 1 or more values
     * /route?one={?}  // allow 0 or 1 values
     * /route?one={[?]]} // allow 0 or more values
     */
    public data class Parameter(val name: String, val optional: Boolean, val allowMultiple: Boolean) : QueryParameter {
        override val paramName: String = name
        override val weight: Int = if (optional) 2 else 4
        override val mustBeAtEnd: Boolean = false
        override val isStatic: Boolean = optional

        override fun matchInDestination(queryParameters: Map<String, List<String>>): MatchResult {
            val matchingParameter = queryParameters[name]
            return if (matchingParameter != null) {
                // we have a matching value
                if (matchingParameter.size > 1) {
                    if (allowMultiple) {
                        // we have multiple parameters for this name, but we are allowing multiples
                        MatchResult.AddParams(mapOf(name to matchingParameter))
                    } else {
                        // we have multiple parameters for this name, when we require at most 1. This is a mismatch
                        MatchResult.Mismatch
                    }
                } else {
                    // we have a single matching value
                    MatchResult.AddParams(mapOf(name to matchingParameter))
                }
            } else {
                // No parameters at this value were given
                if (optional) {
                    // but it's optional, so consider it a match, still
                    MatchResult.Match(name)
                } else {
                    // and we require a value, so this is a mismatch
                    MatchResult.Mismatch
                }
            }
        }

        override fun directions(queryParameters: Map<String, List<String>>): Directions {
            val parameterValue = queryParameters[name]

            return when {
                parameterValue == null -> {
                    if (optional) {
                        Directions(
                            toAppend = emptyMap(),
                            consumedParameterNames = emptySet(),
                        )
                    } else {
                        Directions(
                            consumedParameterNames = setOf(name),
                            error = "Non-optional query parameter '$name' must be provided"
                        )
                    }
                }

                parameterValue.isEmpty() -> {
                    if (optional) {
                        Directions(
                            toAppend = emptyMap(),
                            consumedParameterNames = setOf(name),
                        )
                    } else {
                        Directions(
                            consumedParameterNames = setOf(name),
                            error = "Non-optional query parameter '$name' must be provided"
                        )
                    }
                }

                parameterValue.size == 1 -> {
                    Directions(
                        toAppend = mapOf(name to listOf(parameterValue.single())),
                        consumedParameterNames = setOf(name),
                    )
                }

                else -> {
                    if (this.allowMultiple) {
                        Directions(
                            toAppend = mapOf(name to parameterValue),
                            consumedParameterNames = setOf(name),
                        )
                    } else {
                        Directions(
                            consumedParameterNames = setOf(name),
                            error = "Query parameter '$name' can only have a single value"
                        )
                    }
                }
            }
        }
    }

    /**
     * /route?{...}
     */
    public object Remainder : QueryParameter {
        override val paramName: String? = null
        override val weight: Int = 1
        override val mustBeAtEnd: Boolean = true
        override val isStatic: Boolean = true

        override fun matchInDestination(queryParameters: Map<String, List<String>>): MatchResult {
            // just return everything that's remaining
            return MatchResult.AddParams(queryParameters)
        }

        override fun directions(queryParameters: Map<String, List<String>>): Directions {
            return Directions(
                toAppend = queryParameters,
                consumedParameterNames = queryParameters.keys,
            )
        }
    }

    public sealed interface MatchResult {
        public object Mismatch : MatchResult
        public data class Match(val name: String) : MatchResult
        public data class AddParams(val queryParameters: Map<String, List<String>>) : MatchResult
    }

    public data class Directions(
        val toAppend: Map<String, List<String>> = emptyMap(),
        val consumedParameterNames: Set<String> = emptySet(),
        val error: String? = null,
    )
}
