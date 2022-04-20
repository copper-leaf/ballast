package com.copperleaf.ballast.examples.scorekeeper

import com.copperleaf.ballast.BallastViewModelConfiguration
import com.copperleaf.ballast.core.AndroidViewModel
import com.copperleaf.ballast.examples.scorekeeper.ScorekeeperContract

class ScorekeeperViewModel(
    config: BallastViewModelConfiguration<
        ScorekeeperContract.Inputs,
        ScorekeeperContract.Events,
        ScorekeeperContract.State>
) : AndroidViewModel<
    ScorekeeperContract.Inputs,
    ScorekeeperContract.Events,
    ScorekeeperContract.State>(
    config = config
)
