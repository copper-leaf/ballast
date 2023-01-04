package com.copperleaf.ballast.debugger.idea.base

import com.copperleaf.ballast.BallastLogger
import com.intellij.openapi.diagnostic.Logger

class IntellijPluginBallastLogger(private val ideaPluginLogger: Logger) : BallastLogger {
    override fun debug(message: String) {
        println(message)
    }

    override fun info(message: String) {
        println(message)
    }

    override fun error(throwable: Throwable) {
        println(throwable)
    }
}
