package com.copperleaf.ballast.core

import com.copperleaf.ballast.BallastLogger

/**
 * A simple logger that can be used on any Kotlin platform. It writes logs to [println] at `info` and `debug` levels,
 * and calls `throwable.printStackTrace()` at the `error` level.
 *
 * If [tag] is provided, it will be prepended to messages printed at `info` and `debug` levels.
 */
public class PrintlnLogger(private val tag: String? = null) : BallastLogger {
    override fun debug(message: String) {
        if (tag != null) {
            println("[$tag] $message")
        } else {
            println(message)
        }
    }

    override fun info(message: String) {
        if (tag != null) {
            println("[$tag] $message")
        } else {
            println(message)
        }
    }

    override fun error(throwable: Throwable) {
        throwable.printStackTrace()
    }
}
