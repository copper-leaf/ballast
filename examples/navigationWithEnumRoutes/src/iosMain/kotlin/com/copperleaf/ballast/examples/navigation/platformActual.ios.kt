package com.copperleaf.ballast.examples.navigation

import com.copperleaf.ballast.BallastViewModelConfiguration
import com.copperleaf.ballast.core.LoggingInterceptor
import com.copperleaf.ballast.core.OSLogLogger
import com.copperleaf.ballast.debugger.BallastDebuggerClientConnection
import com.copperleaf.ballast.debugger.BallastDebuggerInterceptor
import com.copperleaf.ballast.navigation.routing.RoutingTable
import com.copperleaf.ballast.navigation.vm.RouterBuilder
import com.copperleaf.ballast.navigation.vm.withRouter
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

@OptIn(ExperimentalForeignApi::class, ExperimentalNativeApi::class)
internal actual fun BallastViewModelConfiguration.Builder.installLogging(): BallastViewModelConfiguration.Builder {
    return apply {
        logger = ::OSLogLogger
        this += LoggingInterceptor()
    }
}

internal actual fun BallastViewModelConfiguration.Builder.installDebugger(): BallastViewModelConfiguration.Builder {
    return apply {
        this += BallastDebuggerInterceptor(lazyConnection)
    }
}

internal actual fun BallastViewModelConfiguration.Builder.installRouting(
    routingTable: RoutingTable<AppScreen>,
    initialRoute: AppScreen,
): RouterBuilder<AppScreen> {
    return withRouter(routingTable, initialRoute)
}
