package com.copperleaf.ballast.examples.bgg.ui

import com.copperleaf.ballast.BallastViewModelConfiguration
import com.copperleaf.ballast.core.BasicViewModel
import com.copperleaf.ballast.examples.bgg.repository.BggRepository
import com.copperleaf.ballast.forViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
class BggViewModel(
    viewModelCoroutineScope: CoroutineScope,
    configurationBuilder: BallastViewModelConfiguration.Builder,
    repository: BggRepository,
) : BasicViewModel<
    BggContract.Inputs,
    BggContract.Events,
    BggContract.State>(
    config = configurationBuilder
        .forViewModel(
            initialState = BggContract.State(),
            inputHandler = BggInputHandler(repository),
            name = "BGG",
        ),
    eventHandler = BggEventHandler(),
    coroutineScope = viewModelCoroutineScope,
)
