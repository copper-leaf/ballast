package com.copperleaf.ballast.core

/**
 * A wrapper around an Exception thrown by something in a Ballast ViewModel.
 */
public data class BallastException(
    val _cause: Throwable,
    val handled: Boolean,
    val latestState: Any?,
    val inputSequence: List<Any>
) : RuntimeException(_cause)
