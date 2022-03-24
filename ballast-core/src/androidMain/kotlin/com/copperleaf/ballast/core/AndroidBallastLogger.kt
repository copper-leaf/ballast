package com.copperleaf.ballast.core

import android.util.Log
import com.copperleaf.ballast.BallastLogger

public class AndroidBallastLogger(private val tag: String) : BallastLogger {
    override fun debug(message: String) {
        Log.d(tag, message)
    }

    override fun info(message: String) {
        Log.i(tag, message)
    }

    override fun error(throwable: Throwable) {
        Log.e(tag, null, throwable)
    }
}
