package com.copperleaf.ballast.examples.util

import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.savedstate.SavedStateRegistryOwner
import com.copperleaf.ballast.examples.MainApplication
import com.copperleaf.ballast.examples.bgg.BggViewModel
import com.copperleaf.ballast.examples.counter.CounterViewModel
import com.copperleaf.ballast.examples.kitchensink.controller.KitchenSinkControllerViewModel
import com.copperleaf.ballast.examples.scorekeeper.ScorekeeperViewModel

@Suppress("UNCHECKED_CAST")
class BallastViewModelFactory(
    owner: SavedStateRegistryOwner
) : AbstractSavedStateViewModelFactory(owner, null) {

    private val injector: AndroidInjector get() = MainApplication.getInstance().injector

    override fun <T : ViewModel> create(key: String, modelClass: Class<T>, handle: SavedStateHandle): T {
        return when(modelClass) {
            BggViewModel::class.java -> injector.bggViewModel() as T
            CounterViewModel::class.java -> injector.counterViewModel(handle) as T
            KitchenSinkControllerViewModel::class.java -> injector.kitchenSinkControllerViewModel() as T
            ScorekeeperViewModel::class.java -> injector.scorekeeperViewModel() as T
            else -> error("$modelClass not supported")
        }
    }

}
