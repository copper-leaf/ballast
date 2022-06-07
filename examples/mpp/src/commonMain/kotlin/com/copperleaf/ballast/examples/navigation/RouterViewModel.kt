package com.copperleaf.ballast.examples.navigation

import com.copperleaf.ballast.BallastViewModelConfiguration
import com.copperleaf.ballast.core.BasicViewModel
import com.copperleaf.ballast.eventHandler
import com.copperleaf.ballast.navigation.routing.RouterContract
import kotlinx.coroutines.CoroutineScope

class RouterViewModel(
    coroutineScope: CoroutineScope,
    config: BallastViewModelConfiguration<
        RouterContract.Inputs,
        RouterContract.Events,
        RouterContract.State>,
) : BasicViewModel<
    RouterContract.Inputs,
    RouterContract.Events,
    RouterContract.State>(
    coroutineScope = coroutineScope,
    config = config,
    eventHandler = eventHandler { },
)
