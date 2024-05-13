package com.copperleaf.ballast.navigation.internal

internal class Uri internal constructor(
    val protocol: String,
    val host: String,
    val port: Int,
    val decodedPathSegments: List<String>,
    val decodedQueryParameters: Map<String, List<String>>,
    val decodedFragment: String?,
) {
    internal val encodedPath: String = buildString {
        if (decodedPathSegments.isNotEmpty()) {
            decodedPathSegments.forEach {
                append('/')
                append(UriEncoder.encodeUrlPathSegment(it))
            }
        }
    }
    internal val encodedQueryString: String = buildString {
        if (decodedQueryParameters.isNotEmpty()) {
            append('?')

            val (head, tail) = decodedQueryParameters
                .flatMap { (key, values) ->
                    values.map { value -> key to value }
                }
                .let { flattenedParams ->
                    flattenedParams.take(1).single() to flattenedParams.drop(1)
                }

            append(head.first)
            append('=')
            append(head.second)

            tail.forEach { (key, value) ->
                append('&')
                append(UriEncoder.encodeUrlQueryComponent(key))
                append('=')
                append(UriEncoder.encodeUrlQueryComponent(value))
            }
        }
    }
    internal val encodedFragment: String = buildString {
        if (!decodedFragment.isNullOrBlank()) {
            append("#")
            append(UriEncoder.encodeUrlPath(decodedFragment))
        }
    }
    internal val encodedPathAndQuery: String = buildString {
        append(encodedPath)
        append(encodedQueryString)
    }

    private val urlString: String = buildString {
        append(protocol)
        append("://")
        append(host)

        val defaultPortForProtocol = protocolDefaultPorts[protocol]
        if (defaultPortForProtocol == null || defaultPortForProtocol != port) {
            append(':')
            append(port.toString())
        }

        append(encodedPathAndQuery)
        append(encodedFragment)
    }

    override fun toString(): String {
        return urlString
    }

    companion object {
        internal val defaultProtocol = "http"
        internal val defaultHost = "localhost"
        internal val protocolDefaultPorts = mapOf(
            "http" to 80,
            "https" to 443,
            "ws" to 80,
            "wss" to 443,
            "socks" to 1080,
        )
    }
}
