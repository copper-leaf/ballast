package com.copperleaf.ballast.examples.ui.undo

import com.copperleaf.ballast.BallastViewModelConfiguration
import com.copperleaf.ballast.core.AndroidViewModel
import kotlinx.coroutines.CoroutineScope

class UndoViewModel(
    config: BallastViewModelConfiguration<
        UndoContract.Inputs,
        UndoContract.Events,
        UndoContract.State>,
    coroutineScope: CoroutineScope,
) : AndroidViewModel<
    UndoContract.Inputs,
    UndoContract.Events,
    UndoContract.State>(
    config = config,
    coroutineScope = coroutineScope,
)
