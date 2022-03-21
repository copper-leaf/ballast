package com.copperleaf.ballast.examples.web.internal

import com.copperleaf.ballast.BallastLogger

class JsConsoleLogger : BallastLogger {
    override fun debug(message: String) {
        console.log(message)
    }

    override fun info(message: String) {
        console.info(message)
    }

    override fun error(throwable: Throwable) {
        console.error(throwable)
    }
}
