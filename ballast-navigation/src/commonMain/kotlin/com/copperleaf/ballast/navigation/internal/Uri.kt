package com.copperleaf.ballast.navigation.internal

import io.ktor.http.ParametersBuilder
import io.ktor.http.Url
import io.ktor.http.decodeURLPart
import io.ktor.http.encodeURLPath
import io.ktor.http.encodeURLPathPart
import io.ktor.http.encodeURLQueryComponent
import io.ktor.http.parseQueryString
import io.ktor.utils.io.charsets.Charset
import io.ktor.utils.io.charsets.Charsets

internal class Uri private constructor(
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
                append(Uri.encodeUrlPathSegment(it))
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
                append(Uri.encodeUrlQueryComponent(key))
                append('=')
                append(Uri.encodeUrlQueryComponent(value))
            }
        }
    }
    internal val encodedFragment: String = buildString {
        if (!decodedFragment.isNullOrBlank()) {
            append("#")
            append(Uri.encodeUrlPath(decodedFragment))
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
        private val defaultProtocol = "http"
        private val defaultHost = "localhost"
        private val protocolDefaultPorts = mapOf(
            "http" to 80,
            "https" to 443,
            "ws" to 80,
            "wss" to 443,
            "socks" to 1080,
        )

        internal fun parse(uri: String): Uri {
            return Url(uri)
                .toUri()
        }

        internal fun Url.toUri(): Uri {
            val parsedUrl = this
            return Uri(
                protocol = parsedUrl.protocol.name,
                host = parsedUrl.host,
                port = parsedUrl.port,
                decodedPathSegments = parsedUrl.pathSegments.dropWhile { it.isBlank() }.dropLastWhile { it.isBlank() },
                decodedQueryParameters = parsedUrl.parameters.entries().associate { it.key to it.value },
                decodedFragment = parsedUrl.fragment.takeIf { it.isNotBlank() },
            )
        }

        internal fun build(
            encodedPath: String,
            encodedQueryString: String?,
        ): Uri {
            val decodedPathSegments = encodedPath
                .split('/')
                .dropWhile { it.isBlank() }.dropLastWhile { it.isBlank() }
                .map { decodeUrlPathSegment(it) }
            val decodedParameters = ParametersBuilder()
                .apply { appendAll(parseQueryString(encodedQueryString ?: "", decode = true)) }
                .entries()
                .associate { it.key to it.value }

            return Uri(
                protocol = defaultProtocol,
                host = defaultHost,
                port = protocolDefaultPorts[defaultProtocol]!!,
                decodedPathSegments = decodedPathSegments,
                decodedQueryParameters = decodedParameters,
                decodedFragment = null,
            )
        }

        internal fun decodeUrlPathSegment(part: String): String {
            return part.decodeURLPart()
        }

        internal fun encodeUrlPathSegment(part: String): String {
            return part.encodeURLPathPart()
        }

        internal fun encodeUrlPath(part: String): String {
            return part.encodeURLPath()
        }

        internal fun encodeUrlQueryComponent(
            queryComponent: String,
            spaceToPlus: Boolean = false,
            charset: Charset = Charsets.UTF_8
        ): String {
            return queryComponent.encodeURLQueryComponent(true, spaceToPlus, charset)
        }

        internal fun encodeUrlQueryString(
            queryComponent: String,
            spaceToPlus: Boolean = false,
            charset: Charset = Charsets.UTF_8
        ): String {
            return queryComponent.encodeURLQueryComponent(false, spaceToPlus, charset)
        }
    }
}
