package com.copperleaf.ballast.examples.navigation

import com.copperleaf.ballast.BallastViewModelConfiguration
import com.copperleaf.ballast.build
import com.copperleaf.ballast.eventHandler
import com.copperleaf.ballast.navigation.vm.BasicRouter
import kotlinx.coroutines.CoroutineScope

class RouterViewModel(
    viewModelCoroutineScope: CoroutineScope
) : BasicRouter<AppScreenRoute>(
    config = BallastViewModelConfiguration.Builder()
        .withAppScreenRouter()
        .build(),
    eventHandler = eventHandler { },
    coroutineScope = viewModelCoroutineScope,
)
