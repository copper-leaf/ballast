package com.copperleaf.ballast.core

import com.copperleaf.ballast.BallastLogger

/**
 * An implementation of a [BallastLogger] which writes log messages to the JavaScript `console`.
 */
public class JsConsoleLogger(private val tag: String? = null) : BallastLogger {
    override fun debug(message: String) {
        console.log(formatMessage(tag, message))
    }

    override fun info(message: String) {
        console.info(formatMessage(tag, message))
    }

    override fun error(throwable: Throwable) {
        console.error(throwable)
    }
}
