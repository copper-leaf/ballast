package com.copperleaf.ballast.examples.navigation

import com.copperleaf.ballast.BallastLogger
import com.copperleaf.ballast.BallastViewModelConfiguration
import com.copperleaf.ballast.navigation.routing.RoutingTable
import com.copperleaf.ballast.navigation.vm.RouterBuilder

internal expect fun BallastViewModelConfiguration.Builder.installLogging(): BallastViewModelConfiguration.Builder

internal expect fun BallastViewModelConfiguration.Builder.installDebugger(): BallastViewModelConfiguration.Builder

internal expect fun BallastViewModelConfiguration.Builder.installRouting(
    routingTable: RoutingTable<AppScreen>,
    initialRoute: AppScreen,
): RouterBuilder<AppScreen>
