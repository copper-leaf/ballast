package com.copperleaf.ballast.core

import com.copperleaf.ballast.BallastLogger

/**
 * A Logger that ignores anything logged by Ballast internals, the [LoggingInterceptor], or custom usages anywhere else.
 * This is the default logger if none is provided to the [DefaultViewModelConfiguration].
 */
public class NoOpLogger : BallastLogger {
    override fun debug(message: String) {}
    override fun info(message: String) {}
    override fun error(throwable: Throwable) {}
}
