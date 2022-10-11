package com.copperleaf.ballast.examples.util

import com.copperleaf.ballast.InputStrategy
import com.copperleaf.ballast.examples.bgg.BggViewModel
import com.copperleaf.ballast.examples.counter.CounterViewModel
import com.copperleaf.ballast.examples.kitchensink.KitchenSinkViewModel
import com.copperleaf.ballast.examples.kitchensink.controller.KitchenSinkControllerViewModel
import com.copperleaf.ballast.examples.scorekeeper.ScorekeeperViewModel
import com.copperleaf.ballast.sync.DefaultSyncConnection
import com.copperleaf.ballast.examples.undo.UndoContract
import com.copperleaf.ballast.examples.undo.UndoViewModel
import com.copperleaf.ballast.undo.UndoController
import kotlinx.coroutines.CoroutineScope

interface ComposeWebInjector {

    fun kitchenSinkControllerViewModel(
        coroutineScope: CoroutineScope,
    ): KitchenSinkControllerViewModel

    fun kitchenSinkViewModel(
        coroutineScope: CoroutineScope,
        inputStrategy: InputStrategy<*, *, *>,
    ): KitchenSinkViewModel

    fun counterViewModel(
        coroutineScope: CoroutineScope,
        syncClientType: DefaultSyncConnection.ClientType?,
    ): CounterViewModel

    fun bggViewModel(
        coroutineScope: CoroutineScope,
    ): BggViewModel

    fun scorekeeperViewModel(
        coroutineScope: CoroutineScope,
    ): ScorekeeperViewModel

    public val undoController: UndoController<
        UndoContract.Inputs,
        UndoContract.Events,
        UndoContract.State>

    fun undoViewModel(
        coroutineScope: CoroutineScope,
    ): UndoViewModel
}
