package com.copperleaf.ballast.examples.navigation

import com.copperleaf.ballast.BallastViewModelConfiguration
import com.copperleaf.ballast.build
import com.copperleaf.ballast.eventHandler
import com.copperleaf.ballast.navigation.routing.RoutingTable
import com.copperleaf.ballast.navigation.routing.fromEnum
import com.copperleaf.ballast.navigation.vm.BasicRouter
import com.copperleaf.ballast.navigation.vm.Router
import kotlinx.coroutines.CoroutineScope

// Build VM
// ---------------------------------------------------------------------------------------------------------------------

internal fun createRouter(viewModelCoroutineScope: CoroutineScope): Router<AppScreen> {
    return BasicRouter(
        config = BallastViewModelConfiguration.Builder()
            .installLogging()
            .installDebugger()
            .installRouting(RoutingTable.fromEnum(AppScreen.entries), AppScreen.Home)
            .build(),
        eventHandler = eventHandler { },
        coroutineScope = viewModelCoroutineScope,
    )
}
