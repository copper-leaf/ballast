package com.copperleaf.ballast.examples.mainlist

import com.copperleaf.ballast.BallastViewModelConfiguration
import com.copperleaf.ballast.core.AndroidViewModel
import com.copperleaf.ballast.examples.mainlist.MainContract

class MainViewModel(
    config: BallastViewModelConfiguration<
        MainContract.Inputs,
        MainContract.Events,
        MainContract.State>,
) : AndroidViewModel<
    MainContract.Inputs,
    MainContract.Events,
    MainContract.State>(
    config = config,
)
