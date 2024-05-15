package com.copperleaf.ballast.navigation.internal

import com.copperleaf.ballast.navigation.internal.Uri.Companion.defaultHost
import com.copperleaf.ballast.navigation.internal.Uri.Companion.defaultProtocol
import com.copperleaf.ballast.navigation.internal.Uri.Companion.protocolDefaultPorts

internal object UriBuilder {
    internal fun parse(uri: String): Uri {
        val parsedUrl = com.eygraber.uri.Uri.parseOrNull(uri)!!

        val protocol = parsedUrl.scheme ?: defaultProtocol
        val decodedPathSegments = parsedUrl
            .pathSegments
            .dropWhile { it.isBlank() }.dropLastWhile { it.isBlank() }
            .map { UriDecoder.decodeUrlPathSegment(it) }
        val decodedParameters = buildMap<String, List<String>> {
            parsedUrl.encodedQuery
                ?.split('&')
                ?.filterNot { it.isBlank() }
                ?.forEach { keyValuePairs ->
                    val (key, value) = keyValuePairs.split('=')
                    val decodedKey = UriDecoder.decodeQueryComponentKey(key)
                    val decodedValue = UriDecoder.decodeQueryComponentValue(value)
                    this[decodedKey] = this.getOrElse(decodedKey) { emptyList() } + decodedValue
                }
        }

        return Uri(
            protocol = protocol,
            host = parsedUrl.host ?: defaultHost,
            port = parsedUrl.port.takeIf { it > 0 } ?: protocolDefaultPorts[protocol]!!,
            decodedPathSegments = decodedPathSegments,
            decodedQueryParameters = decodedParameters,
            decodedFragment = parsedUrl.fragment.takeIf { !it.isNullOrBlank() },
        )
    }

    internal fun build(
        encodedPath: String,
        encodedQueryString: String?,
    ): Uri {
        val decodedPathSegments = encodedPath
            .split('/')
            .dropWhile { it.isBlank() }.dropLastWhile { it.isBlank() }
            .map { UriDecoder.decodeUrlPathSegment(it) }
        val decodedParameters = buildMap<String, List<String>> {
            encodedQueryString
                ?.split('&')
                ?.filterNot { it.isBlank() }
                ?.forEach { keyValuePairs ->
                    val (key, value) = keyValuePairs.split('=')
                    val decodedKey = UriDecoder.decodeQueryComponentKey(key)
                    val decodedValue = UriDecoder.decodeQueryComponentValue(value)
                    this[decodedKey] = this.getOrElse(decodedKey) { emptyList() } + decodedValue
                }
        }

        return Uri(
            protocol = defaultProtocol,
            host = defaultHost,
            port = protocolDefaultPorts[defaultProtocol]!!,
            decodedPathSegments = decodedPathSegments,
            decodedQueryParameters = decodedParameters,
            decodedFragment = null,
        )
    }
}
