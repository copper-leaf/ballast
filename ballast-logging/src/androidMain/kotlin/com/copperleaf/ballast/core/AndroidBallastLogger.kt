package com.copperleaf.ballast.core

import android.util.Log
import com.copperleaf.ballast.BallastLogger

@Deprecated(
    "Use AndroidLogger instead. Deprecated since v3, to be removed in v4.",
    replaceWith = ReplaceWith("AndroidLogger", "com.copperleaf.ballast.core.AndroidLogger")
)
/**
 * An implementation of a [BallastLogger] which writes log messages to the Android `Log`.
 *
 * This class has been deprecated, and you should use AndroidLogger instead to keep more consistent naming of Loggers.
 * Deprecated since v3, to be removed in v4.
 */
public class AndroidBallastLogger(private val tag: String = "Ballast") : BallastLogger {
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