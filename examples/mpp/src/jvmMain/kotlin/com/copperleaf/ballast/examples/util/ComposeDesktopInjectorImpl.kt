package com.copperleaf.ballast.examples.util

import androidx.compose.material.SnackbarHostState
import com.copperleaf.ballast.InputStrategy
import com.copperleaf.ballast.core.PrintlnLogger
import com.copperleaf.ballast.examples.bgg.BggViewModel
import com.copperleaf.ballast.examples.bgg.ui.BggEventHandler
import com.copperleaf.ballast.examples.counter.CounterEventHandler
import com.copperleaf.ballast.examples.counter.CounterViewModel
import com.copperleaf.ballast.examples.kitchensink.KitchenSinkEventHandler
import com.copperleaf.ballast.examples.kitchensink.KitchenSinkViewModel
import com.copperleaf.ballast.examples.kitchensink.controller.KitchenSinkControllerEventHandler
import com.copperleaf.ballast.examples.kitchensink.controller.KitchenSinkControllerViewModel
import com.copperleaf.ballast.examples.mainlist.MainEventHandler
import com.copperleaf.ballast.examples.mainlist.MainViewModel
import com.copperleaf.ballast.examples.navigation.RouterViewModel
import com.copperleaf.ballast.examples.scorekeeper.ScorekeeperEventHandler
import com.copperleaf.ballast.examples.scorekeeper.ScorekeeperViewModel
import com.copperleaf.ballast.sync.SyncClientType
import io.ktor.client.engine.HttpClientEngineConfig
import io.ktor.client.engine.okhttp.OkHttp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope

class ComposeDesktopInjectorImpl(
    private val applicationScope: CoroutineScope,
) : CommonBallastInjector<HttpClientEngineConfig>(
    applicationScope = applicationScope,
    engineFactory = OkHttp,
    bggApi = ::BggApiImpl,
    loggerFactory = { PrintlnLogger() },
    debuggerHost = "127.0.0.1",
), ComposeDesktopInjector {

    private val router = RouterViewModel(MainScope(), routerConfiguration())

    override fun routerViewModel(): RouterViewModel {
        return router
    }

    override fun mainViewModel(coroutineScope: CoroutineScope): MainViewModel {
        return MainViewModel(
            coroutineScope = coroutineScope,
            config = mainConfiguration(),
            eventHandler = MainEventHandler(routerViewModel())
        )
    }

    override fun kitchenSinkControllerViewModel(
        coroutineScope: CoroutineScope,
    ): KitchenSinkControllerViewModel {
        return KitchenSinkControllerViewModel(
            viewModelCoroutineScope = coroutineScope,
            config = kitchenSinkControllerConfiguration(),
            eventHandler = KitchenSinkControllerEventHandler(routerViewModel()),
        )
    }

    override fun kitchenSinkViewModel(
        coroutineScope: CoroutineScope,
        inputStrategy: InputStrategy,
    ): KitchenSinkViewModel {
        return KitchenSinkViewModel(
            viewModelCoroutineScope = coroutineScope,
            config = kitchenSinkConfiguration(inputStrategy),
            eventHandler = KitchenSinkEventHandler(routerViewModel()),
        )
    }

    override fun counterViewModel(
        coroutineScope: CoroutineScope,
        syncClientType: SyncClientType,
    ): CounterViewModel {
        return CounterViewModel(
            viewModelCoroutineScope = coroutineScope,
            config = counterConfiguration(syncClientType, null),
            eventHandler = CounterEventHandler(routerViewModel()),
        )
    }

    override fun bggViewModel(coroutineScope: CoroutineScope): BggViewModel {
        return BggViewModel(
            viewModelCoroutineScope = coroutineScope,
            config = bggConfiguration(),
            eventHandler = BggEventHandler(routerViewModel()),
        )
    }

    override fun scorekeeperViewModel(
        coroutineScope: CoroutineScope,
        snackbarHostState: SnackbarHostState,
    ): ScorekeeperViewModel {
        return ScorekeeperViewModel(
            viewModelCoroutineScope = coroutineScope,
            config = scorekeeperConfiguration(),
            eventHandler = ScorekeeperEventHandler(routerViewModel()) { snackbarHostState.showSnackbar(it) },
        )
    }
}
