package com.copperleaf.ballast.core

import android.util.Log
import com.copperleaf.ballast.BallastLogger

/**
 * An implementation of a [BallastLogger] which writes log messages to the Android `Log`.
 */
public class AndroidLogger(private val tag: String = "Ballast") : BallastLogger {
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
