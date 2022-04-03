package com.copperleaf.ballast.core

import com.copperleaf.ballast.BallastLogger

public class PrintlnLogger(private val tag: String? = null) : BallastLogger {
    override fun debug(message: String) {
        if(tag != null) {
            println("[$tag] $message")
        } else {
            println(message)
        }
    }

    override fun info(message: String) {
        if(tag != null) {
            println("[$tag] $message")
        } else {
            println(message)
        }
    }

    override fun error(throwable: Throwable) {
        throwable.printStackTrace()
    }
}
