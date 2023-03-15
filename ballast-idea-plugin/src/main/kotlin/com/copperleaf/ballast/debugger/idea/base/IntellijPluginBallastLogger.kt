package com.copperleaf.ballast.debugger.idea.base

import com.copperleaf.ballast.BallastLogger
import com.copperleaf.ballast.core.PrintlnLogger
import com.intellij.openapi.diagnostic.Logger

class IntellijPluginBallastLogger(
    private val tag: String
) : BallastLogger {
    private val ideaPluginLogger: Logger = Logger.getInstance(tag)
    private val printlnDelegate = PrintlnLogger(tag)

    override fun debug(message: String) {
        printlnDelegate.debug(message)
    }

    override fun info(message: String) {
        printlnDelegate.info(message)
    }

    override fun error(throwable: Throwable) {
        printlnDelegate.error(throwable)
    }
}
