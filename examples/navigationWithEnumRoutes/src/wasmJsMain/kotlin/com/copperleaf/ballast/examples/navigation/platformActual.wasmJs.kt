package com.copperleaf.ballast.examples.navigation

import com.copperleaf.ballast.BallastViewModelConfiguration
import com.copperleaf.ballast.core.LoggingInterceptor
import com.copperleaf.ballast.core.WasmJsConsoleLogger
import com.copperleaf.ballast.navigation.browser.withBrowserHashRouter
import com.copperleaf.ballast.navigation.routing.RoutingTable
import com.copperleaf.ballast.navigation.vm.RouterBuilder
import com.copperleaf.ballast.plusAssign

internal actual fun BallastViewModelConfiguration.Builder.installLogging(): BallastViewModelConfiguration.Builder {
    return apply {
        logger = ::WasmJsConsoleLogger
        this += LoggingInterceptor()
    }
}

internal actual fun BallastViewModelConfiguration.Builder.installDebugger(): BallastViewModelConfiguration.Builder {
    return apply {
    }
}

internal actual fun BallastViewModelConfiguration.Builder.installRouting(
    routingTable: RoutingTable<AppScreen>,
    initialRoute: AppScreen,
): RouterBuilder<AppScreen> {
    return withBrowserHashRouter(routingTable, initialRoute)
}
