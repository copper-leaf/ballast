package com.copperleaf.ballast.examples.kitchensink.controller

import com.copperleaf.ballast.examples.util.ExamplesPreferences
import com.copperleaf.ballast.savedstate.RestoreStateScope
import com.copperleaf.ballast.savedstate.SaveStateScope
import com.copperleaf.ballast.savedstate.SavedStateAdapter

class KitchenSinkControllerSavedStateAdapter(
    private val prefs: ExamplesPreferences,
) : SavedStateAdapter<
    KitchenSinkControllerContract.Inputs,
    KitchenSinkControllerContract.Events,
    KitchenSinkControllerContract.State> {

    override suspend fun SaveStateScope<
        KitchenSinkControllerContract.Inputs,
        KitchenSinkControllerContract.Events,
        KitchenSinkControllerContract.State>.save() {
        saveDiff({ inputStrategy }) { inputStrategy ->
            prefs.kitchenSinkInputStrategySelection = inputStrategy
        }
    }

    override suspend fun RestoreStateScope<
        KitchenSinkControllerContract.Inputs,
        KitchenSinkControllerContract.Events,
        KitchenSinkControllerContract.State>.restore(): KitchenSinkControllerContract.State {
        return KitchenSinkControllerContract.State(
            inputStrategy = prefs.kitchenSinkInputStrategySelection
        )
    }

    override suspend fun onRestoreComplete(restoredState: KitchenSinkControllerContract.State): KitchenSinkControllerContract.Inputs? {
        return KitchenSinkControllerContract.Inputs.UpdateInputStrategy(restoredState.inputStrategy)
    }
}
