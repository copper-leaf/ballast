package com.copperleaf.ballast.examples.injector

import com.copperleaf.ballast.BallastViewModelConfiguration
import com.copperleaf.ballast.build
import com.copperleaf.ballast.core.JsConsoleBallastLogger
import com.copperleaf.ballast.core.KillSwitch
import com.copperleaf.ballast.core.LoggingInterceptor
import com.copperleaf.ballast.debugger.BallastDebuggerClientConnection
import com.copperleaf.ballast.debugger.BallastDebuggerInterceptor
import com.copperleaf.ballast.examples.api.BggApiImpl
import com.copperleaf.ballast.examples.preferences.ExamplesPreferencesImpl
import com.copperleaf.ballast.examples.repository.BggRepositoryContract
import com.copperleaf.ballast.examples.repository.BggRepositoryImpl
import com.copperleaf.ballast.examples.repository.BggRepositoryInputHandler
import com.copperleaf.ballast.examples.router.BallastExamples
import com.copperleaf.ballast.examples.router.BallastExamplesRouter
import com.copperleaf.ballast.examples.ui.bgg.BggContract
import com.copperleaf.ballast.examples.ui.bgg.BggEventHandler
import com.copperleaf.ballast.examples.ui.bgg.BggInputHandler
import com.copperleaf.ballast.examples.ui.bgg.BggViewModel
import com.copperleaf.ballast.examples.ui.counter.CounterContract
import com.copperleaf.ballast.examples.ui.counter.CounterEventHandler
import com.copperleaf.ballast.examples.ui.counter.CounterInputHandler
import com.copperleaf.ballast.examples.ui.counter.CounterViewModel
import com.copperleaf.ballast.examples.ui.kitchensink.InputStrategySelection
import com.copperleaf.ballast.examples.ui.kitchensink.KitchenSinkContract
import com.copperleaf.ballast.examples.ui.kitchensink.KitchenSinkEventHandler
import com.copperleaf.ballast.examples.ui.kitchensink.KitchenSinkInputHandler
import com.copperleaf.ballast.examples.ui.kitchensink.KitchenSinkViewModel
import com.copperleaf.ballast.examples.ui.scorekeeper.ScorekeeperContract
import com.copperleaf.ballast.examples.ui.scorekeeper.ScorekeeperEventHandler
import com.copperleaf.ballast.examples.ui.scorekeeper.ScorekeeperInputHandler
import com.copperleaf.ballast.examples.ui.scorekeeper.ScorekeeperSavedStateAdapter
import com.copperleaf.ballast.examples.ui.scorekeeper.ScorekeeperViewModel
import com.copperleaf.ballast.examples.ui.undo.UndoContract
import com.copperleaf.ballast.examples.ui.undo.UndoEventHandler
import com.copperleaf.ballast.examples.ui.undo.UndoInputHandler
import com.copperleaf.ballast.examples.ui.undo.UndoViewModel
import com.copperleaf.ballast.navigation.browser.BrowserHashNavigationInterceptor
import com.copperleaf.ballast.navigation.routing.RoutingTable
import com.copperleaf.ballast.navigation.routing.fromEnum
import com.copperleaf.ballast.navigation.vm.Router
import com.copperleaf.ballast.navigation.vm.withRouter
import com.copperleaf.ballast.plusAssign
import com.copperleaf.ballast.repository.bus.EventBusImpl
import com.copperleaf.ballast.repository.withRepository
import com.copperleaf.ballast.savedstate.BallastSavedStateInterceptor
import com.copperleaf.ballast.sync.BallastSyncInterceptor
import com.copperleaf.ballast.sync.DefaultSyncConnection
import com.copperleaf.ballast.sync.SyncConnectionAdapter
import com.copperleaf.ballast.undo.BallastUndoInterceptor
import com.copperleaf.ballast.undo.UndoController
import com.copperleaf.ballast.withViewModel
import com.russhwolf.settings.Settings
import io.ktor.client.HttpClient
import io.ktor.client.engine.js.Js
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.onEach
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

class ComposeWebInjectorImpl(
    private val applicationScope: CoroutineScope,
    private val useBrowserHashes: Boolean,
    private val initialRoute: BallastExamples,
) : ComposeWebInjector {

// Router
// ---------------------------------------------------------------------------------------------------------------------

    private val router by lazy {
        BallastExamplesRouter(
            viewModelCoroutineScope = applicationScope,
            config = commonBuilder()
                .withRouter(RoutingTable.fromEnum(BallastExamples.values()), null)
                .apply {
                    if (useBrowserHashes) {
                        interceptors += BrowserHashNavigationInterceptor<BallastExamples>(initialRoute)
                    }
                }
                .build(),
        )
    }

    override fun router(): Router<BallastExamples> {
        return router
    }

// Counter
// ---------------------------------------------------------------------------------------------------------------------

    override fun counterViewModel(
        coroutineScope: CoroutineScope,
        syncClientType: DefaultSyncConnection.ClientType?,
        syncAdapter: SyncConnectionAdapter<
                CounterContract.Inputs,
                CounterContract.Events,
                CounterContract.State>?,
    ): CounterViewModel {
        return CounterViewModel(
            viewModelCoroutineScope = coroutineScope,
            config = commonBuilder()
                .apply {
                    if (syncClientType != null && syncAdapter != null) {
                        this += BallastSyncInterceptor(
                            connection = DefaultSyncConnection(
                                clientType = syncClientType,
                                adapter = syncAdapter,
                                bufferStates = { it.onEach { delay(500.milliseconds) } },
                                bufferInputs = { it.onEach { delay(500.milliseconds) } },
                            ),
                        )
                    }
                }
                .withViewModel(
                    initialState = CounterContract.State(),
                    inputHandler = CounterInputHandler(),
                    name = "Counter",
                )
                .build(),
            eventHandler = CounterEventHandler(),
        )
    }

// Scorekeeper
// ---------------------------------------------------------------------------------------------------------------------

    private val preferences = ExamplesPreferencesImpl(Settings())

    override fun scorekeeperViewModel(
        coroutineScope: CoroutineScope,
    ): ScorekeeperViewModel {
        return ScorekeeperViewModel(
            viewModelCoroutineScope = coroutineScope,
            config = commonBuilder()
                .apply {
                    this += BallastSavedStateInterceptor(
                        ScorekeeperSavedStateAdapter(preferences)
                    )
                }
                .withViewModel(
                    initialState = ScorekeeperContract.State(),
                    inputHandler = ScorekeeperInputHandler(),
                    name = "Scorekeeper",
                )
                .build(),
            eventHandler = ScorekeeperEventHandler(),
        )
    }

// Undo
// ---------------------------------------------------------------------------------------------------------------------

    override fun undoViewModel(
        coroutineScope: CoroutineScope,
        undoController: UndoController<
                UndoContract.Inputs,
                UndoContract.Events,
                UndoContract.State>
    ): UndoViewModel {
        return UndoViewModel(
            viewModelCoroutineScope = coroutineScope,
            config = commonBuilder()
                .apply {
                    this += BallastUndoInterceptor(undoController)
                }
                .withViewModel(
                    initialState = UndoContract.State(),
                    inputHandler = UndoInputHandler(),
                    name = "Undo",
                )
                .build(),
            eventHandler = UndoEventHandler(undoController),
        )
    }

// BGG API Call/Cache
// ---------------------------------------------------------------------------------------------------------------------

    private val eventBus = EventBusImpl()

    private val httpClient = HttpClient(Js) {
        install(Logging) {
            level = LogLevel.BODY
            logger = object : Logger {
                override fun log(message: String) {
                    JsConsoleBallastLogger("Ktor").info(message)
                }
            }
        }
    }

    private val bggRepository by lazy {
        BggRepositoryImpl(
            coroutineScope = applicationScope,
            eventBus = eventBus,
            config = commonBuilder()
                .withViewModel(
                    inputHandler = BggRepositoryInputHandler(
                        eventBus = eventBus,
                        api = BggApiImpl(httpClient),
                    ),
                    initialState = BggRepositoryContract.State(),
                    name = "Bgg Repository",
                )
                .withRepository()
                .build(),
        )
    }

    override fun bggViewModel(coroutineScope: CoroutineScope): BggViewModel {
        return BggViewModel(
            viewModelCoroutineScope = coroutineScope,
            config = commonBuilder()
                .withViewModel(
                    initialState = BggContract.State(),
                    inputHandler = BggInputHandler(bggRepository),
                    name = "BGG",
                )
                .build(),
            eventHandler = BggEventHandler(),
        )
    }

// Kitchen Sink
// ---------------------------------------------------------------------------------------------------------------------

    override fun kitchenSinkViewModel(
        coroutineScope: CoroutineScope,
        inputStrategy: InputStrategySelection,
    ): KitchenSinkViewModel {
        val killSwitch = KillSwitch<
                KitchenSinkContract.Inputs,
                KitchenSinkContract.Events,
                KitchenSinkContract.State>(5.seconds)
        return KitchenSinkViewModel(
            viewModelCoroutineScope = coroutineScope,
            config = commonBuilder()
                .apply {
                    this.inputStrategy = inputStrategy.get()
                    this += killSwitch
                }
                .withViewModel(
                    initialState = KitchenSinkContract.State(inputStrategy = inputStrategy),
                    inputHandler = KitchenSinkInputHandler(killSwitch),
                    name = "KitchenSink",
                )
                .build(),
            eventHandler = KitchenSinkEventHandler(router),
        )
    }

// configs
// ---------------------------------------------------------------------------------------------------------------------

    private val debuggerConnection by lazy {
        BallastDebuggerClientConnection(
            engineFactory = Js,
            applicationCoroutineScope = applicationScope,
            host = "127.0.0.1",
        ).also {
            it.connect()
        }
    }

    private fun commonBuilder(): BallastViewModelConfiguration.Builder {
        return BallastViewModelConfiguration.Builder()
            .apply {
                this += LoggingInterceptor()
                this += BallastDebuggerInterceptor(debuggerConnection)
                logger = ::JsConsoleBallastLogger
            }
    }
}
