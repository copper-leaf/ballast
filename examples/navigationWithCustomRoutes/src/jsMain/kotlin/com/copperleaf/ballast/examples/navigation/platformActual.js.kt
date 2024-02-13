package com.copperleaf.ballast.examples.navigation

import com.copperleaf.ballast.BallastLogger
import com.copperleaf.ballast.core.JsConsoleLogger
import com.copperleaf.ballast.debugger.BallastDebuggerClientConnection
import io.ktor.client.engine.js.Js
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

private val lazyConnection by lazy {
    BallastDebuggerClientConnection(
        engineFactory = Js,
        applicationCoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
        host = "127.0.0.1",
    ) {
        // CIO Ktor client engine configuration
    }.also { it.connect() }
}

internal actual fun platformDebuggerConnection(): BallastDebuggerClientConnection<*> {
    return lazyConnection
}

internal actual fun platformLogger(loggerName: String): BallastLogger {
    return JsConsoleLogger(loggerName)
}
