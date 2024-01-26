package com.copperleaf.ballast.examples.navigation

import com.copperleaf.ballast.BallastViewModelConfiguration
import com.copperleaf.ballast.build
import com.copperleaf.ballast.core.LoggingInterceptor
import com.copperleaf.ballast.debugger.BallastDebuggerInterceptor
import com.copperleaf.ballast.eventHandler
import com.copperleaf.ballast.navigation.routing.RoutingTable
import com.copperleaf.ballast.navigation.routing.fromEnum
import com.copperleaf.ballast.navigation.vm.BasicRouter
import com.copperleaf.ballast.navigation.vm.Router
import com.copperleaf.ballast.navigation.vm.withRouter
import com.copperleaf.ballast.plusAssign
import kotlinx.coroutines.CoroutineScope

// Build VM
// ---------------------------------------------------------------------------------------------------------------------

internal fun createRouter(viewModelCoroutineScope: CoroutineScope): Router<AppScreen> {
    return BasicRouter(
        config = BallastViewModelConfiguration.Builder()
            .logging()
            .debugging()
            .withRouter(RoutingTable.fromEnum(AppScreen.entries), AppScreen.Home)
            .build(),
        eventHandler = eventHandler { },
        coroutineScope = viewModelCoroutineScope,
    )
}

private fun BallastViewModelConfiguration.Builder.logging(): BallastViewModelConfiguration.Builder = apply {
    logger = ::platformLogger
    this += LoggingInterceptor()
}

private fun BallastViewModelConfiguration.Builder.debugging(): BallastViewModelConfiguration.Builder = apply {
    this += BallastDebuggerInterceptor(platformDebuggerConnection())
}
