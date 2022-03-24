package com.copperleaf.ballast.debugger.idea

import com.copperleaf.ballast.BallastLogger
import com.intellij.openapi.diagnostic.Logger

class IntellijPluginBallastLogger(private val ideaPluginLogger: Logger) : BallastLogger {
    override fun debug(message: String) {
        ideaPluginLogger.debug(message)
    }

    override fun info(message: String) {
        ideaPluginLogger.info(message)
    }

    override fun error(throwable: Throwable) {
        ideaPluginLogger.error(throwable)
    }
}
