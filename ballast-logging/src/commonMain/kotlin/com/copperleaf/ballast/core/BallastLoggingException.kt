package com.copperleaf.ballast.core

/**
 * A wrapper around an Exception thrown by something in a Ballast ViewModel.
 */
@Suppress("DEPRECATION")
public class BallastLoggingException(
    _cause: Throwable,
    handled: Boolean,
    latestState: Any?,
    inputSequence: List<Any>
) : BallastException(_cause, handled, latestState, inputSequence)
