@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package com.copperleaf.ballast.examples.scheduler

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

actual class Notifications actual constructor() {
    actual fun notify(
        title: String,
        message: String,
    ) { }

    actual fun getNotificationLogs(): List<String> {
        return emptyList()
    }
}
