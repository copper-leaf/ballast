package com.copperleaf.ballast.core

import com.copperleaf.ballast.BallastLogger

/**
 * An implementation of a [BallastLogger] which writes log messages to [println], rather than to a platform-specific
 * logger. This logger can be used on any Kotlin platform.
 */
public class PrintlnLogger(private val tag: String? = null) : BallastLogger {
    override fun debug(message: String) {
        println(formatMessage(tag, message))
    }

    override fun info(message: String) {
        println(formatMessage(tag, message))
    }

    override fun error(throwable: Throwable) {
        throwable.printStackTrace()
    }
}
