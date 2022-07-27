package com.copperleaf.ballast.navigation.routing

public sealed interface PathSegment {

    public fun matchDestinationPathSegments(
        pathSegments: List<String>,
        currentIndex: Int,
    ): Int

    /**
     * /route/one
     */
    public data class Static(val text: String) : PathSegment {
        override fun matchDestinationPathSegments(pathSegments: List<String>, currentIndex: Int): Int {
            return if(pathSegments[currentIndex] == text) {
                1
            } else {
                -1
            }
        }
    }

    /**
     * /route/\*
     */
    public object Wildcard : PathSegment {
        override fun matchDestinationPathSegments(pathSegments: List<String>, currentIndex: Int): Int {
            return 1
        }
    }

    /**
     * /route/:one
     * /route/{one}
     * /route/{one?}
     */
    public data class Parameter(val name: String, val optional: Boolean) : PathSegment {
        override fun matchDestinationPathSegments(pathSegments: List<String>, currentIndex: Int): Int {
            return if (optional) {
                if (pathSegments.getOrNull(currentIndex) != null) {
                    1
                } else {
                    0
                }
            } else {
                1
            }
        }
    }

    /**
     * /route/{...}
     * /route/{one...}
     */
    public data class Tailcard(val name: String?) : PathSegment {
        override fun matchDestinationPathSegments(pathSegments: List<String>, currentIndex: Int): Int {
            return pathSegments.size - currentIndex
        }
    }
}
