package com.copperleaf.ballast.examples.bgg

import com.copperleaf.ballast.BallastViewModelConfiguration
import com.copperleaf.ballast.core.AndroidViewModel
import com.copperleaf.ballast.examples.bgg.ui.BggContract

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
