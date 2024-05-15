package com.copperleaf.ballast.navigation.internal

import com.eygraber.uri.UriCodec

internal object UriDecoder {
    internal fun decodeUrlPathSegment(part: String): String {
        return UriCodec.decode(part, convertPlus = false)
    }

    internal fun decodeQueryComponentKey(component: String): String {
        return UriCodec.decode(component, convertPlus = false)
    }

    internal fun decodeQueryComponentValue(component: String): String {
        return UriCodec.decode(component, convertPlus = true)
    }
}
