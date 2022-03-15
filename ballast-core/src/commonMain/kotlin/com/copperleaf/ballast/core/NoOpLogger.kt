package com.copperleaf.ballast.core

import com.copperleaf.ballast.BallastLogger

public class NoOpLogger : BallastLogger {
    override fun debug(message: String) {}
    override fun info(message: String) {}
    override fun error(throwable: Throwable) {}
}
