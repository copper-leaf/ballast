package com.copperleaf.ballast.examples.scorekeeper

import com.copperleaf.ballast.BallastViewModelConfiguration
import com.copperleaf.ballast.core.BasicViewModel
import com.copperleaf.ballast.examples.scorekeeper.prefs.ScoreKeeperPrefs
import com.copperleaf.ballast.forViewModel
import kotlinx.coroutines.CoroutineScope

class ScorekeeperViewModel(
    viewModelCoroutineScope: CoroutineScope,
    configurationBuilder: BallastViewModelConfiguration.Builder,
    prefs: ScoreKeeperPrefs,
    displayErrorMessage: (String) -> Unit
) : BasicViewModel<
    ScorekeeperContract.Inputs,
    ScorekeeperContract.Events,
    ScorekeeperContract.State>(
    config = configurationBuilder
        .forViewModel(
            initialState = ScorekeeperContract.State(),
            inputHandler = ScorekeeperInputHandler(prefs),
            name = "Scorekeeper",
        ),
    eventHandler = ScorekeeperEventHandler(displayErrorMessage),
    coroutineScope = viewModelCoroutineScope,
)
