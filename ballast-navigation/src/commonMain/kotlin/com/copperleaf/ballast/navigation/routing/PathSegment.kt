package com.copperleaf.ballast.navigation.routing

/**
 * The types of Path Segments which may be parsed from a Route URL format.
 */
public sealed interface PathSegment {

    public val paramName: String?
    public val weight: Int
    public val mustBeAtEnd: Boolean
    public val isStatic: Boolean

    public fun matchInDestination(
        pathSegments: List<String>,
        currentIndex: Int,
    ): MatchResult

    public fun directions(
        pathParameters: Map<String, List<String>>,
    ): Directions

    /**
     * /route/one
     */
    public data class Static(val text: String) : PathSegment {
        override val paramName: String? = null
        override val weight: Int = 5
        override val mustBeAtEnd: Boolean = false
        override val isStatic: Boolean = true

        override fun matchInDestination(pathSegments: List<String>, currentIndex: Int): MatchResult {
            return if (currentIndex in pathSegments.indices) {
                if (pathSegments[currentIndex] == text) {
                    // consume the static text of the path segment
                    MatchResult.Match(1)
                } else {
                    // do not consume anything
                    MatchResult.Mismatch
                }
            } else {
                // there's nothing left to consume, this is a mismatch
                MatchResult.Mismatch
            }
        }

        override fun directions(pathParameters: Map<String, List<String>>): Directions {
            return Directions(
                toAppend = listOf(text),
                consumedParameterName = null,
            )
        }
    }

    /**
     * /route/\*
     */
    public object Wildcard : PathSegment {
        override val mustBeAtEnd: Boolean = false
        override val paramName: String? = null
        override val weight: Int = 3
        override val isStatic: Boolean = false

        override fun matchInDestination(pathSegments: List<String>, currentIndex: Int): MatchResult {
            return if (currentIndex in pathSegments.indices) {
                // consume the static text of the path segment
                MatchResult.Match(1)
            } else {
                // there's nothing left to consume, this is a mismatch
                MatchResult.Mismatch
            }
        }

        override fun directions(pathParameters: Map<String, List<String>>): Directions {
            return Directions(
                error = "cannot generate directions for a wildcard parameter. Consider using a named parameter instead."
            )
        }
    }

    /**
     * /route/:one
     * /route/{one}
     * /route/{one?}
     */
    public data class Parameter(val name: String, val optional: Boolean) : PathSegment {
        override val mustBeAtEnd: Boolean = optional
        override val paramName: String = name
        override val weight: Int = if (optional) 2 else 4
        override val isStatic: Boolean = optional

        override fun matchInDestination(pathSegments: List<String>, currentIndex: Int): MatchResult {
            return if (currentIndex in pathSegments.indices) {
                // consume the text of the current path segment, and add it as a parameter
                MatchResult.AddParam(name, listOf(pathSegments[currentIndex]))
            } else {
                // there's nothing left to consume...
                if (optional) {
                    // ... but that's fine, this parameter is optional
                    MatchResult.Match(0)
                } else {
                    // ... but since it's not optional, this is a mismatch
                    MatchResult.Mismatch
                }
            }
        }

        override fun directions(pathParameters: Map<String, List<String>>): Directions {
            val parameterValue = pathParameters[name]

            return when {
                parameterValue == null -> {
                    if (optional) {
                        Directions(
                            toAppend = emptyList(),
                            consumedParameterName = null,
                        )
                    } else {
                        Directions(
                            consumedParameterName = name,
                            error = "Non-optional path parameter '$name' must be provided"
                        )
                    }
                }

                parameterValue.isEmpty() -> {
                    if (optional) {
                        Directions(
                            toAppend = emptyList(),
                            consumedParameterName = name,
                        )
                    } else {
                        Directions(
                            consumedParameterName = name,
                            error = "Non-optional path parameter '$name' must be provided"
                        )
                    }
                }

                parameterValue.size == 1 -> {
                    Directions(
                        toAppend = listOf(parameterValue.single()),
                        consumedParameterName = name,
                    )
                }

                else -> {
                    Directions(
                        consumedParameterName = name,
                        error = "Path parameter '$name' must have a single value"
                    )
                }
            }
        }
    }

    /**
     * /route/{...}
     * /route/{one...}
     */
    public data class Tailcard(val name: String?) : PathSegment {
        override val paramName: String? = name
        override val weight: Int = 1
        override val mustBeAtEnd: Boolean = true
        override val isStatic: Boolean = false

        override fun matchInDestination(pathSegments: List<String>, currentIndex: Int): MatchResult {
            return if (name != null) {
                // we care about the value of the tailcard, so add it as a parameter
                MatchResult.AddParam(name, pathSegments.subList(currentIndex, pathSegments.size))
            } else {
                // we don't care about the value, just match whatever is there
                MatchResult.Match(pathSegments.size - currentIndex)
            }
        }

        override fun directions(pathParameters: Map<String, List<String>>): Directions {
            return if (name == null) {
                Directions(
                    toAppend = emptyList(),
                    consumedParameterName = null,
                )
            } else {
                val parameterValue = pathParameters[name]

                when {
                    parameterValue.isNullOrEmpty() -> {
                        Directions(
                            toAppend = emptyList(),
                            consumedParameterName = null,
                        )
                    }

                    else -> {
                        Directions(
                            toAppend = parameterValue,
                            consumedParameterName = name,
                        )
                    }
                }
            }
        }
    }

    public sealed interface MatchResult {
        public object Mismatch : MatchResult
        public data class Match(val numberOfMatchedSegments: Int) : MatchResult
        public data class AddParam(val name: String, val values: List<String>) : MatchResult
    }

    public data class Directions(
        val toAppend: List<String> = emptyList(),
        val consumedParameterName: String? = null,
        val error: String? = null
    )
}
