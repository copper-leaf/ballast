package com.copperleaf.ballast.examples.util

import androidx.lifecycle.SavedStateHandle
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

interface AndroidInjector {

    fun kitchenSinkControllerViewModel(): KitchenSinkControllerViewModel

    fun kitchenSinkViewModel(
        coroutineScope: CoroutineScope,
        inputStrategy: InputStrategy<*, *, *>,
    ): KitchenSinkViewModel

    fun counterViewModel(
        savedStateHandle: SavedStateHandle?,
        syncClientType: DefaultSyncConnection.ClientType?,
    ): CounterViewModel

    fun bggViewModel(): BggViewModel

    fun scorekeeperViewModel(): ScorekeeperViewModel

    public val undoController: UndoController<
        UndoContract.Inputs,
        UndoContract.Events,
        UndoContract.State>

    fun undoViewModel(): UndoViewModel
}
