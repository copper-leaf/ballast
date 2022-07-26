package com.copperleaf.ballast.examples.util

import com.copperleaf.ballast.InputStrategy
import com.copperleaf.ballast.examples.bgg.BggViewModel
import com.copperleaf.ballast.examples.counter.CounterViewModel
import com.copperleaf.ballast.examples.kitchensink.KitchenSinkViewModel
import com.copperleaf.ballast.examples.kitchensink.controller.KitchenSinkControllerViewModel
import com.copperleaf.ballast.examples.mainlist.MainViewModel
import com.copperleaf.ballast.examples.navigation.RouterViewModel
import com.copperleaf.ballast.examples.scorekeeper.ScorekeeperViewModel
import com.copperleaf.ballast.sync.SyncClientType
import kotlinx.coroutines.CoroutineScope

interface ComposeWebInjector {

    fun routerViewModel(): RouterViewModel

    fun mainViewModel(
        coroutineScope: CoroutineScope
    ): MainViewModel

    fun kitchenSinkControllerViewModel(
        coroutineScope: CoroutineScope,
    ): KitchenSinkControllerViewModel

    fun kitchenSinkViewModel(
        coroutineScope: CoroutineScope,
        inputStrategy: InputStrategy,
    ): KitchenSinkViewModel

    fun counterViewModel(
        coroutineScope: CoroutineScope,
        syncClientType: SyncClientType,
    ): CounterViewModel

    fun bggViewModel(
        coroutineScope: CoroutineScope,
    ): BggViewModel

    fun scorekeeperViewModel(
        coroutineScope: CoroutineScope,
    ): ScorekeeperViewModel
}
