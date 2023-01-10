package com.copperleaf.ballast.test.internal

import com.copperleaf.ballast.BallastLogger

public class SimpleTestLogger : BallastLogger {
    // ignore debug messages
    override fun debug(message: String) {}

    // show info messages
    override fun info(message: String) {
        println(message)
    }

    // ignore errors
    override fun error(throwable: Throwable) {
        throwable.printStackTrace()
    }
}
