/**
 * Taken from https://touchlab.co/wasm-in-kermit
 */
package com.copperleaf.ballast.core

import com.copperleaf.ballast.BallastLogger

@JsFun("(output) => console.log(output)")
internal external fun consoleLog(vararg output: String?)

@JsFun("(output) => console.info(output)")
internal external fun consoleInfo(vararg output: String?)

@JsFun("(output) => console.error(output)")
internal external fun consoleError(vararg output: String?)

/**
 * An implementation of a [BallastLogger] which writes log messages to the JavaScript `console`.
 */
public class WasmJsConsoleLogger(private val tag: String? = null) : BallastLogger {
    override fun debug(message: String) {
        consoleLog(formatMessage(tag, message))
    }

    override fun info(message: String) {
        consoleInfo(formatMessage(tag, message))
    }

    override fun error(throwable: Throwable) {
        consoleError(throwable.stackTraceToString())
    }
}
