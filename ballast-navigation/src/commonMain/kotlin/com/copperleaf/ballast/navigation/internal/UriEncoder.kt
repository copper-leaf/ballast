package com.copperleaf.ballast.navigation.internal

import com.eygraber.uri.UriCodec

internal object UriEncoder {
    internal fun encodeUrlPathSegment(part: String): String {
        return UriCodec.encode(part, allow = "+")
    }

    internal fun encodeUrlPath(part: String): String {
        return UriCodec.encode(part, allow = "+/")
    }

    internal fun encodeUrlQueryComponent(
        queryComponent: String,
        spaceToPlus: Boolean = false,
    ): String {
        return if(spaceToPlus) {
            UriCodec.encode(queryComponent)
                .replace("%20", "+")
        }  else {
            UriCodec.encode(queryComponent)
        }.replace(".", "%2E")
    }

    internal fun encodeUrlQueryString(
        queryComponent: String,
        spaceToPlus: Boolean = false,
    ): String {
        return if(spaceToPlus) {
            UriCodec.encode(queryComponent, allow = "?/=&")
                .replace("%20", "+")
        }  else {
            UriCodec.encode(queryComponent, allow = "?/=&")
        }.replace("%2E", ".")
    }
}
