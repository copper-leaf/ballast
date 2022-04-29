package com.copperleaf.ballast.examples.util

import androidx.lifecycle.SavedStateHandle
import com.copperleaf.ballast.InputStrategy
import com.copperleaf.ballast.core.AndroidBallastLogger
import com.copperleaf.ballast.examples.bgg.BggViewModel
import com.copperleaf.ballast.examples.counter.CounterSavedStateAdapter
import com.copperleaf.ballast.examples.counter.CounterViewModel
import com.copperleaf.ballast.examples.kitchensink.KitchenSinkEventHandler
import com.copperleaf.ballast.examples.kitchensink.KitchenSinkViewModel
import com.copperleaf.ballast.examples.kitchensink.controller.KitchenSinkControllerViewModel
import com.copperleaf.ballast.examples.scorekeeper.ScorekeeperViewModel
import io.ktor.client.engine.HttpClientEngineConfig
import io.ktor.client.engine.okhttp.OkHttp
import kotlinx.coroutines.CoroutineScope

class AndroidInjectorImpl(
    applicationScope: CoroutineScope,
) : CommonBallastInjector<HttpClientEngineConfig>(
    applicationScope = applicationScope,
    engineFactory = OkHttp,
    bggApi = ::BggApiImpl,
    loggerFactory = ::AndroidBallastLogger,
    debuggerHost = "10.0.2.2",
), AndroidInjector {

    override fun kitchenSinkControllerViewModel(): KitchenSinkControllerViewModel {
        return KitchenSinkControllerViewModel(
            kitchenSinkControllerConfiguration()
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

    override fun counterViewModel(
        savedStateHandle: SavedStateHandle
    ): CounterViewModel {
        return CounterViewModel(
            counterConfiguration(CounterSavedStateAdapter(savedStateHandle)),
        )
    }

    override fun bggViewModel(): BggViewModel {
        return BggViewModel(
            bggConfiguration()
        )
    }

    override fun scorekeeperViewModel(): ScorekeeperViewModel {
        return ScorekeeperViewModel(
            scorekeeperConfiguration()
        )
    }
}
