package com.copperleaf.ballast.autoscale

import com.copperleaf.ballast.BallastViewModel
import kotlinx.coroutines.CoroutineScope

public interface ViewModelFactory<Inputs : Any, Events : Any, State : Any> {
    public fun createViewModel(
        coroutineScope: CoroutineScope,
        id: Int,
    ): BallastViewModel<Inputs, Events, State>
}
