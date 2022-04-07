package com.copperleaf.ballast.examples.util

import androidx.compose.material.SnackbarHostState
import com.copperleaf.ballast.InputStrategy
import com.copperleaf.ballast.core.PrintlnLogger
import com.copperleaf.ballast.examples.bgg.ui.BggEventHandler
import com.copperleaf.ballast.examples.counter.CounterEventHandler
import com.copperleaf.ballast.examples.bgg.BggViewModel
import com.copperleaf.ballast.examples.counter.CounterViewModel
import com.copperleaf.ballast.examples.kitchensink.KitchenSinkViewModel
import com.copperleaf.ballast.examples.kitchensink.controller.KitchenSinkControllerViewModel
import com.copperleaf.ballast.examples.scorekeeper.ScorekeeperViewModel
import com.copperleaf.ballast.examples.kitchensink.KitchenSinkEventHandler
import com.copperleaf.ballast.examples.kitchensink.controller.KitchenSinkControllerEventHandler
import com.copperleaf.ballast.examples.scorekeeper.ScorekeeperEventHandler
import io.ktor.client.engine.HttpClientEngineConfig
import io.ktor.client.engine.okhttp.OkHttp
import kotlinx.coroutines.CoroutineScope

class ComposeDesktopInjectorImpl(
    private val applicationScope: CoroutineScope,
) : CommonBallastInjector<HttpClientEngineConfig>(
    applicationScope = applicationScope,
    engineFactory = OkHttp,
    bggApi = ::BggApiImpl,
    loggerFactory = { PrintlnLogger() },
), ComposeDesktopInjector {

    override fun kitchenSinkControllerViewModel(
        coroutineScope: CoroutineScope,
    ): KitchenSinkControllerViewModel {
        return KitchenSinkControllerViewModel(
            viewModelCoroutineScope = coroutineScope,
            config = kitchenSinkControllerConfiguration(),
            eventHandler = KitchenSinkControllerEventHandler(),
        )
    }

    override fun kitchenSinkViewModel(
        coroutineScope: CoroutineScope,
        inputStrategy: InputStrategy,
    ): KitchenSinkViewModel {
        return KitchenSinkViewModel(
            viewModelCoroutineScope = coroutineScope,
            config = kitchenSinkConfiguration(inputStrategy),
            eventHandler = KitchenSinkEventHandler { /* ignore */ },
        )
    }

    override fun counterViewModel(coroutineScope: CoroutineScope): CounterViewModel {
        return CounterViewModel(
            viewModelCoroutineScope = coroutineScope,
            config = counterConfiguration(null),
            eventHandler = CounterEventHandler(),
        )
    }

    override fun bggViewModel(coroutineScope: CoroutineScope): BggViewModel {
        return BggViewModel(
            viewModelCoroutineScope = coroutineScope,
            config = bggConfiguration(),
            eventHandler = BggEventHandler(),
        )
    }

    override fun scorekeeperViewModel(
        coroutineScope: CoroutineScope,
        snackbarHostState: SnackbarHostState,
    ): ScorekeeperViewModel {
        return ScorekeeperViewModel(
            viewModelCoroutineScope = coroutineScope,
            config = scorekeeperConfiguration(),
            eventHandler = ScorekeeperEventHandler { snackbarHostState.showSnackbar(it) },
        )
    }
}
