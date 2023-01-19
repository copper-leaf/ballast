package com.copperleaf.ballast.examples.injector

import androidx.fragment.app.Fragment
import androidx.lifecycle.SavedStateHandle
import com.copperleaf.ballast.examples.router.BallastExamplesRouter
import com.copperleaf.ballast.examples.router.BallastExamplesRouterEventHandler
import com.copperleaf.ballast.examples.ui.MainActivity
import com.copperleaf.ballast.examples.ui.bgg.BggEventHandler
import com.copperleaf.ballast.examples.ui.bgg.BggViewModel
import com.copperleaf.ballast.examples.ui.counter.CounterContract
import com.copperleaf.ballast.examples.ui.counter.CounterEventHandler
import com.copperleaf.ballast.examples.ui.counter.CounterViewModel
import com.copperleaf.ballast.examples.ui.kitchensink.InputStrategySelection
import com.copperleaf.ballast.examples.ui.kitchensink.KitchenSinkEventHandler
import com.copperleaf.ballast.examples.ui.kitchensink.KitchenSinkViewModel
import com.copperleaf.ballast.examples.ui.scorekeeper.ScorekeeperEventHandler
import com.copperleaf.ballast.examples.ui.scorekeeper.ScorekeeperViewModel
import com.copperleaf.ballast.examples.ui.undo.UndoContract
import com.copperleaf.ballast.examples.ui.undo.UndoEventHandler
import com.copperleaf.ballast.examples.ui.undo.UndoViewModel
import com.copperleaf.ballast.sync.DefaultSyncConnection
import com.copperleaf.ballast.sync.SyncConnectionAdapter
import com.copperleaf.ballast.undo.state.StateBasedUndoController

interface AndroidInjector {

// Router
// ---------------------------------------------------------------------------------------------------------------------

    fun router(): BallastExamplesRouter

    fun routerEventHandler(activity: MainActivity): BallastExamplesRouterEventHandler

// Counter
// ---------------------------------------------------------------------------------------------------------------------

    fun counterViewModel(
        savedStateHandle: SavedStateHandle?,
        syncClientType: DefaultSyncConnection.ClientType?,
        syncAdapter: SyncConnectionAdapter<
                CounterContract.Inputs,
                CounterContract.Events,
                CounterContract.State>?,
    ): CounterViewModel

    fun counterEventHandler(fragment: Fragment): CounterEventHandler

// Scorekeeper
// ---------------------------------------------------------------------------------------------------------------------

    fun scorekeeperViewModel(): ScorekeeperViewModel

    fun scorekeeperEventHandler(fragment: Fragment): ScorekeeperEventHandler

// Undo
// ---------------------------------------------------------------------------------------------------------------------

    fun undoViewModel(
        undoController: StateBasedUndoController<
                UndoContract.Inputs,
                UndoContract.Events,
                UndoContract.State>,
    ): UndoViewModel

    fun undoEventHandler(
        fragment: Fragment,
        undoController: StateBasedUndoController<
            UndoContract.Inputs,
            UndoContract.Events,
            UndoContract.State>,
    ): UndoEventHandler

// BGG API Call/Cache
// ---------------------------------------------------------------------------------------------------------------------

    fun bggViewModel(): BggViewModel

    fun bggEventHandler(fragment: Fragment): BggEventHandler

// Kitchen Sink
// ---------------------------------------------------------------------------------------------------------------------

    fun kitchenSinkViewModel(
        inputStrategy: InputStrategySelection,
    ): KitchenSinkViewModel

    fun kitchenSinkEventHandler(fragment: Fragment): KitchenSinkEventHandler
}
