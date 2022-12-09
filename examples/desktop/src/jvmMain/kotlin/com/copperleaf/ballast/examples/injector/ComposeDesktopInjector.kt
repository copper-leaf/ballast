package com.copperleaf.ballast.examples.injector

import androidx.compose.material.SnackbarHostState
import com.copperleaf.ballast.examples.router.BallastExamples
import com.copperleaf.ballast.examples.ui.bgg.BggViewModel
import com.copperleaf.ballast.examples.ui.counter.CounterContract
import com.copperleaf.ballast.examples.ui.counter.CounterViewModel
import com.copperleaf.ballast.examples.ui.kitchensink.InputStrategySelection
import com.copperleaf.ballast.examples.ui.kitchensink.KitchenSinkViewModel
import com.copperleaf.ballast.examples.ui.scorekeeper.ScorekeeperViewModel
import com.copperleaf.ballast.examples.ui.undo.UndoContract
import com.copperleaf.ballast.examples.ui.undo.UndoViewModel
import com.copperleaf.ballast.navigation.routing.RouterContract
import com.copperleaf.ballast.navigation.vm.Router
import com.copperleaf.ballast.sync.DefaultSyncConnection
import com.copperleaf.ballast.sync.SyncConnectionAdapter
import com.copperleaf.ballast.undo.UndoController
import kotlinx.coroutines.CoroutineScope

interface ComposeDesktopInjector {

// Router
// ---------------------------------------------------------------------------------------------------------------------

    fun routerUndoController(): UndoController<
            RouterContract.Inputs<BallastExamples>,
            RouterContract.Events<BallastExamples>,
            RouterContract.State<BallastExamples>,
            >
    fun router(): Router<BallastExamples>

// Counter
// ---------------------------------------------------------------------------------------------------------------------

    fun counterViewModel(
        coroutineScope: CoroutineScope,
        syncClientType: DefaultSyncConnection.ClientType?,
        syncAdapter: SyncConnectionAdapter<
                CounterContract.Inputs,
                CounterContract.Events,
                CounterContract.State>?,
    ): CounterViewModel

// Scorekeeper
// ---------------------------------------------------------------------------------------------------------------------

    fun scorekeeperViewModel(
        coroutineScope: CoroutineScope,
        snackbarHostState: SnackbarHostState,
    ): ScorekeeperViewModel

// Undo
// ---------------------------------------------------------------------------------------------------------------------'

    fun undoViewModel(
        coroutineScope: CoroutineScope,
        undoController: UndoController<
            UndoContract.Inputs,
            UndoContract.Events,
            UndoContract.State>
    ): UndoViewModel

// BGG API Call/Cache
// ---------------------------------------------------------------------------------------------------------------------

    fun bggViewModel(
        coroutineScope: CoroutineScope,
    ): BggViewModel

// Kitchen Sink
// ---------------------------------------------------------------------------------------------------------------------

    fun kitchenSinkViewModel(
        coroutineScope: CoroutineScope,
        inputStrategy: InputStrategySelection,
    ): KitchenSinkViewModel
}
