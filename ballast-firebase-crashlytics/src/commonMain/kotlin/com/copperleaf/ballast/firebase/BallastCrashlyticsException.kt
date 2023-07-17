@file:Suppress("DEPRECATION")

package com.copperleaf.ballast.firebase

/**
 * A wrapper around an Exception thrown by something in a Ballast ViewModel.
 */
public class BallastCrashlyticsException(
    public val _cause: Throwable,
    public val handled: Boolean,
) : RuntimeException(_cause) {

    @Suppress("DEPRECATION")
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is BallastCrashlyticsException) return false

        if (_cause != other._cause) return false
        if (handled != other.handled) return false

        return true
    }

    override fun hashCode(): Int {
        var result = _cause.hashCode()
        result = 31 * result + handled.hashCode()
        return result
    }
}
