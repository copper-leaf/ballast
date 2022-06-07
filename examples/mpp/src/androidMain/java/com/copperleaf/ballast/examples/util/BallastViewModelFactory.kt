package com.copperleaf.ballast.examples.util

import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.savedstate.SavedStateRegistryOwner
import com.copperleaf.ballast.examples.bgg.BggViewModel
import com.copperleaf.ballast.examples.counter.CounterViewModel
import com.copperleaf.ballast.examples.kitchensink.controller.KitchenSinkControllerViewModel
import com.copperleaf.ballast.examples.mainlist.MainViewModel
import com.copperleaf.ballast.examples.scorekeeper.ScorekeeperViewModel

@Suppress("UNCHECKED_CAST")
class BallastViewModelFactory(
    owner: SavedStateRegistryOwner,
    private val injector: AndroidInjector,
) : AbstractSavedStateViewModelFactory(owner, null) {

    override fun <T : ViewModel?> create(key: String, modelClass: Class<T>, handle: SavedStateHandle): T {
        return when(modelClass) {
            MainViewModel::class.java -> injector.mainViewModel() as T
            BggViewModel::class.java -> injector.bggViewModel() as T
            CounterViewModel::class.java -> injector.counterViewModel(handle) as T
            KitchenSinkControllerViewModel::class.java -> injector.kitchenSinkControllerViewModel() as T
            ScorekeeperViewModel::class.java -> injector.scorekeeperViewModel() as T
            else -> error("$modelClass not supported")
        }
    }

}
