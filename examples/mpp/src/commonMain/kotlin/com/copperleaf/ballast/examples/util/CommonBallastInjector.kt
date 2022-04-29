package com.copperleaf.ballast.examples.util

import com.copperleaf.ballast.BallastLogger
import com.copperleaf.ballast.BallastViewModelConfiguration
import com.copperleaf.ballast.InputStrategy
import com.copperleaf.ballast.core.LoggingInterceptor
import com.copperleaf.ballast.debugger.BallastDebuggerClientConnection
import com.copperleaf.ballast.debugger.BallastDebuggerInterceptor
import com.copperleaf.ballast.examples.bgg.api.BggApi
import com.copperleaf.ballast.examples.bgg.repository.BggRepositoryImpl
import com.copperleaf.ballast.examples.bgg.ui.BggContract
import com.copperleaf.ballast.examples.bgg.ui.BggInputHandler
import com.copperleaf.ballast.examples.counter.CounterContract
import com.copperleaf.ballast.examples.counter.CounterInputHandler
import com.copperleaf.ballast.examples.kitchensink.KitchenSinkContract
import com.copperleaf.ballast.examples.kitchensink.KitchenSinkInputHandler
import com.copperleaf.ballast.examples.kitchensink.KitchenSinkViewModel
import com.copperleaf.ballast.examples.kitchensink.controller.KitchenSinkControllerContract
import com.copperleaf.ballast.examples.kitchensink.controller.KitchenSinkControllerInputHandler
import com.copperleaf.ballast.examples.kitchensink.controller.KitchenSinkControllerSavedStateAdapter
import com.copperleaf.ballast.examples.scorekeeper.ScorekeeperContract
import com.copperleaf.ballast.examples.scorekeeper.ScorekeeperInputHandler
import com.copperleaf.ballast.examples.scorekeeper.ScorekeeperSavedStateAdapter
import com.copperleaf.ballast.forViewModel
import com.copperleaf.ballast.plusAssign
import com.copperleaf.ballast.repository.bus.EventBusImpl
import com.copperleaf.ballast.savedstate.BallastSavedStateInterceptor
import com.copperleaf.ballast.savedstate.SavedStateAdapter
import com.russhwolf.settings.Settings
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngineConfig
import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.features.logging.LogLevel
import io.ktor.client.features.logging.Logger
import io.ktor.client.features.logging.Logging
import kotlinx.coroutines.CoroutineScope

abstract class CommonBallastInjector<out T : HttpClientEngineConfig>(
    private val applicationScope: CoroutineScope,
    private val engineFactory: HttpClientEngineFactory<T>,
    private val bggApi: (HttpClient) -> BggApi,
    private val loggerFactory: (String) -> BallastLogger,
    private val debuggerHost: String,
) {
    private val preferences = ExamplesPreferencesImpl(Settings())
    private val httpClient = HttpClient(engineFactory) {
        install(Logging) {
            level = LogLevel.BODY
            logger = object : Logger {
                override fun log(message: String) {
                    loggerFactory("Ktor").info(message)
                }
            }
        }
    }
    private val debuggerConnection by lazy {
        BallastDebuggerClientConnection(
            engineFactory = engineFactory,
            applicationCoroutineScope = applicationScope,
            host = debuggerHost,
        ).also {
            println("Start trying to connect to debugger")
            it.connect()
        }
    }
    protected val eventBus = EventBusImpl()
    protected val bggRepository by lazy {
        BggRepositoryImpl(
            coroutineScope = applicationScope,
            eventBus = eventBus,
            configBuilder = commonBuilder(),
            api = bggApi(httpClient),
        )
    }

    protected fun commonBuilder(): BallastViewModelConfiguration.Builder {
        return BallastViewModelConfiguration.Builder()
            .apply {
                this += LoggingInterceptor()
                this += BallastDebuggerInterceptor(debuggerConnection)
                logger = loggerFactory
            }
    }

    protected fun kitchenSinkControllerConfiguration(): BallastViewModelConfiguration<
        KitchenSinkControllerContract.Inputs,
        KitchenSinkControllerContract.Events,
        KitchenSinkControllerContract.State> = commonBuilder()
        .apply {
            this += BallastSavedStateInterceptor(
                KitchenSinkControllerSavedStateAdapter(preferences)
            )
        }
        .forViewModel(
            initialState = KitchenSinkControllerContract.State(),
            inputHandler = KitchenSinkControllerInputHandler(::kitchenSinkViewModel),
            name = "KitchenSink Controller",
        )

    abstract fun kitchenSinkViewModel(coroutineScope: CoroutineScope, inputStrategy: InputStrategy): KitchenSinkViewModel

    protected fun kitchenSinkConfiguration(
        inputStrategy: InputStrategy,
    ): BallastViewModelConfiguration<
        KitchenSinkContract.Inputs,
        KitchenSinkContract.Events,
        KitchenSinkContract.State> = commonBuilder()
        .apply {
            this.inputStrategy = inputStrategy
        }
        .forViewModel(
            initialState = KitchenSinkContract.State(),
            inputHandler = KitchenSinkInputHandler(),
            name = "KitchenSink",
        )

    protected fun counterConfiguration(
        adapter: SavedStateAdapter<
            CounterContract.Inputs,
            CounterContract.Events,
            CounterContract.State>?,
    ): BallastViewModelConfiguration<
        CounterContract.Inputs,
        CounterContract.Events,
        CounterContract.State> = commonBuilder()
        .apply {
            if(adapter != null) {
                this += BallastSavedStateInterceptor(adapter)
            }
        }
        .forViewModel(
            initialState = CounterContract.State(),
            inputHandler = CounterInputHandler(),
            name = "Counter",
        )

    protected fun bggConfiguration(): BallastViewModelConfiguration<
        BggContract.Inputs,
        BggContract.Events,
        BggContract.State> = commonBuilder()
        .forViewModel(
            initialState = BggContract.State(),
            inputHandler = BggInputHandler(bggRepository),
            name = "BGG",
        )

    protected fun scorekeeperConfiguration(): BallastViewModelConfiguration<
        ScorekeeperContract.Inputs,
        ScorekeeperContract.Events,
        ScorekeeperContract.State> = commonBuilder()
        .apply {
            this += BallastSavedStateInterceptor(
                ScorekeeperSavedStateAdapter(preferences)
            )
        }
        .forViewModel(
            initialState = ScorekeeperContract.State(),
            inputHandler = ScorekeeperInputHandler(),
            name = "Scorekeeper",
        )
}
