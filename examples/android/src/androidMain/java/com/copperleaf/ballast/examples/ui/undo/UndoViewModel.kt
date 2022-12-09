package com.copperleaf.ballast.examples.ui.undo

import com.copperleaf.ballast.BallastViewModelConfiguration
import com.copperleaf.ballast.core.AndroidViewModel

class UndoViewModel(
    config: BallastViewModelConfiguration<
        UndoContract.Inputs,
        UndoContract.Events,
        UndoContract.State>,
) : AndroidViewModel<
    UndoContract.Inputs,
    UndoContract.Events,
    UndoContract.State>(
    config = config
)
