package com.copperleaf.ballast.core

import com.copperleaf.ballast.BallastLogger

public class PrintlnLogger : BallastLogger {
    override fun debug(message: String) { println(message) }
    override fun info(message: String) { println(message) }
    override fun error(throwable: Throwable) { throwable.printStackTrace() }
}
