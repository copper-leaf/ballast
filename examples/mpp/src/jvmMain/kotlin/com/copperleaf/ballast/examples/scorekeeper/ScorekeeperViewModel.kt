package com.copperleaf.ballast.examples.scorekeeper

import com.copperleaf.ballast.BallastViewModelConfiguration
import com.copperleaf.ballast.core.BasicViewModel
import kotlinx.coroutines.CoroutineScope

class ScorekeeperViewModel(
    viewModelCoroutineScope: CoroutineScope,
    config: BallastViewModelConfiguration<
        ScorekeeperContract.Inputs,
        ScorekeeperContract.Events,
        ScorekeeperContract.State>,
    eventHandler: ScorekeeperEventHandler,
) : BasicViewModel<
    ScorekeeperContract.Inputs,
    ScorekeeperContract.Events,
    ScorekeeperContract.State>(
    config = config,
    eventHandler = eventHandler,
    coroutineScope = viewModelCoroutineScope,
)
