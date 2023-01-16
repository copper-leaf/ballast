@file:Suppress("DEPRECATION")
package com.copperleaf.ballast.firebase

import com.copperleaf.ballast.core.BallastException

/**
 * A wrapper around an Exception thrown by something in a Ballast ViewModel.
 */
public class BallastCrashlyticsException(
    _cause: Throwable,
    handled: Boolean,
) : BallastException(_cause, handled, "[redacted]", emptyList())
