package com.copperleaf.ballast.examples.ui.bgg

import com.copperleaf.ballast.BallastViewModelConfiguration
import com.copperleaf.ballast.core.AndroidViewModel

class BggViewModel(
    config: BallastViewModelConfiguration<
        BggContract.Inputs,
        BggContract.Events,
        BggContract.State>
) : AndroidViewModel<
    BggContract.Inputs,
    BggContract.Events,
    BggContract.State>(
    config = config,
)
