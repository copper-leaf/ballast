package com.copperleaf.ballast.examples.counter

import com.copperleaf.ballast.BallastLogger
import com.copperleaf.ballast.BallastViewModelConfiguration
import com.copperleaf.ballast.core.OSLogLogger
import com.copperleaf.ballast.debugger.BallastDebuggerClientConnection
import com.copperleaf.ballast.debugger.BallastDebuggerInterceptor
import com.copperleaf.ballast.plusAssign
import io.ktor.client.engine.darwin.Darwin
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlin.experimental.ExperimentalNativeApi

private val lazyConnection by lazy {
    BallastDebuggerClientConnection(
        engineFactory = Darwin,
        applicationCoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
        host = "127.0.0.1",
    ) {
        // CIO Ktor client engine configuration
    }.also { it.connect() }
}

internal actual fun BallastViewModelConfiguration.Builder.installDebugger(): BallastViewModelConfiguration.Builder =
    apply {
        this += BallastDebuggerInterceptor(lazyConnection)
    }

@OptIn(ExperimentalForeignApi::class, ExperimentalNativeApi::class)
internal actual fun platformLogger(loggerName: String): BallastLogger {
    return OSLogLogger(loggerName)
}
