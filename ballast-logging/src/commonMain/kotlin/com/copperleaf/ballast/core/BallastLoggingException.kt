@file:Suppress("DEPRECATION")

package com.copperleaf.ballast.core

/**
 * A wrapper around an Exception thrown by something in a Ballast ViewModel.
 */
public class BallastLoggingException(
    public val _cause: Throwable,
    public val handled: Boolean,
    public val latestState: Any?,
    public val inputSequence: List<Any>
) : RuntimeException(_cause) {

    @Suppress("DEPRECATION")
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is BallastLoggingException) return false

        if (_cause != other._cause) return false
        if (handled != other.handled) return false
        if (latestState != other.latestState) return false
        if (inputSequence != other.inputSequence) return false

        return true
    }

    override fun hashCode(): Int {
        var result = _cause.hashCode()
        result = 31 * result + handled.hashCode()
        result = 31 * result + (latestState?.hashCode() ?: 0)
        result = 31 * result + inputSequence.hashCode()
        return result
    }
}
