package com.copperleaf.ballast

/**
 * A simple logging facade for the Ballast library. Should be implemented by your application's normal logger.
 */
public interface BallastLogger {
    public fun debug(message: String) {}
    public fun info(message: String) {}
    public fun error(throwable: Throwable) {}
}
