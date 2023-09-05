package com.copperleaf.ballast.navigation

import android.os.Bundle
import com.copperleaf.ballast.navigation.routing.Destination

private const val PATH_PARAMETERS = "pathParameters"
private const val QUERY_PARAMETERS = "queryParameters"
private const val ERROR_MESSAGE = "Bundle must be created with `Destination.Match<T>.toBundle()`"

public fun Destination.Parameters.toBundle(): Bundle {
    return Bundle().apply {
        putBundle(PATH_PARAMETERS, pathParameters.toParametersBundle())
        putBundle(QUERY_PARAMETERS, queryParameters.toParametersBundle())
    }
}

public fun Bundle.toDestinationParameters(): Destination.Parameters {
    return BundleDestinationParameters(this)
}

private class BundleDestinationParameters(
    private val bundle: Bundle
) : Destination.Parameters {
    override val pathParameters = bundle.getBundle(PATH_PARAMETERS)?.fromParametersBundle() ?: error(ERROR_MESSAGE)
    override val queryParameters = bundle.getBundle(QUERY_PARAMETERS)?.fromParametersBundle() ?: error(ERROR_MESSAGE)
}

private fun Map<String, List<String>>.toParametersBundle(): Bundle {
    return Bundle().apply {
        for((key, values) in entries) {
            putStringArray(key, values.toTypedArray())
        }
    }
}

private fun Bundle.fromParametersBundle(): Map<String, List<String>> {
    val bundle = this
    return buildMap {
        for(key in bundle.keySet()) {
            put(key, bundle.getStringArray(key)?.toList() ?: error(ERROR_MESSAGE))
        }
    }
}
