package com.copperleaf.ballast.examples.ui.undo

import com.copperleaf.ballast.BallastViewModelConfiguration
import com.copperleaf.ballast.core.BasicViewModel
import kotlinx.coroutines.CoroutineScope

class UndoViewModel(
    viewModelCoroutineScope: CoroutineScope,
    config: BallastViewModelConfiguration<
        UndoContract.Inputs,
        UndoContract.Events,
        UndoContract.State>,
    eventHandler: UndoEventHandler
) : BasicViewModel<
    UndoContract.Inputs,
    UndoContract.Events,
    UndoContract.State>(
    config = config,
    eventHandler = eventHandler,
    coroutineScope = viewModelCoroutineScope,
)
