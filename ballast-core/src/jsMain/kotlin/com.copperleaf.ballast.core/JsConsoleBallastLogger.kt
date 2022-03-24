package com.copperleaf.ballast.core

import com.copperleaf.ballast.BallastLogger

public class JsConsoleBallastLogger : BallastLogger {
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
