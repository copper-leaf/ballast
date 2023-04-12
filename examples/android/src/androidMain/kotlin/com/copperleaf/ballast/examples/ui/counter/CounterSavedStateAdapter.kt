package com.copperleaf.ballast.examples.ui.counter

import androidx.lifecycle.SavedStateHandle
import com.copperleaf.ballast.savedstate.AndroidSavedStateAdapter
import com.copperleaf.ballast.savedstate.RestoreStateScope
import com.copperleaf.ballast.savedstate.SaveStateScope

class CounterSavedStateAdapter(
    override val savedStateHandle: SavedStateHandle,
) : AndroidSavedStateAdapter<
    CounterContract.Inputs,
    CounterContract.Events,
    CounterContract.State> {

    override suspend fun SaveStateScope<CounterContract.Inputs, CounterContract.Events, CounterContract.State>.save() {
        saveDiffToSavedStateHandle("count") { count }
    }

    override suspend fun RestoreStateScope<CounterContract.Inputs, CounterContract.Events, CounterContract.State>.restore(): CounterContract.State {
        return CounterContract.State(
            count = get("count") { 0 }
        )
    }
}
