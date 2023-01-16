package com.copperleaf.ballast.core

import com.copperleaf.ballast.BallastLogger

public class JsConsoleBallastLogger(private val tag: String? = null) : BallastLogger {
    override fun debug(message: String) {
        if (tag != null) {
            console.log("[$tag] $message")
        } else {
            console.log(message)
        }
    }

    override fun info(message: String) {
        if (tag != null) {
            console.info("[$tag] $message")
        } else {
            console.info(message)
        }
    }

    override fun error(throwable: Throwable) {
        console.error(throwable)
    }
}
