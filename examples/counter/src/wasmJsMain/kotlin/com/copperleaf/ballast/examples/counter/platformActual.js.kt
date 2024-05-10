package com.copperleaf.ballast.examples.counter

import com.copperleaf.ballast.BallastLogger
import com.copperleaf.ballast.BallastViewModelConfiguration
import com.copperleaf.ballast.core.WasmJsConsoleLogger

internal actual fun BallastViewModelConfiguration.Builder.installDebugger(): BallastViewModelConfiguration.Builder =
    apply {
        // debugger not yet available on wasmJs
    }

internal actual fun platformLogger(loggerName: String): BallastLogger {
    return WasmJsConsoleLogger(loggerName)
}
