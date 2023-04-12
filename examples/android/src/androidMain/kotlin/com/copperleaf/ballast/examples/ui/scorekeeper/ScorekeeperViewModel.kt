package com.copperleaf.ballast.examples.ui.scorekeeper

import com.copperleaf.ballast.BallastViewModelConfiguration
import com.copperleaf.ballast.core.AndroidViewModel
import kotlinx.coroutines.CoroutineScope

class ScorekeeperViewModel(
    config: BallastViewModelConfiguration<
        ScorekeeperContract.Inputs,
        ScorekeeperContract.Events,
        ScorekeeperContract.State>,
    coroutineScope: CoroutineScope,
) : AndroidViewModel<
    ScorekeeperContract.Inputs,
    ScorekeeperContract.Events,
    ScorekeeperContract.State>(
    config = config,
    coroutineScope = coroutineScope,
)
