package com.copperleaf.ballast.core

public data class BallastException(
    val _cause: Throwable,
    val handled: Boolean,
    val latestState: Any?,
    val inputSequence: List<Any>
) : RuntimeException(_cause)
