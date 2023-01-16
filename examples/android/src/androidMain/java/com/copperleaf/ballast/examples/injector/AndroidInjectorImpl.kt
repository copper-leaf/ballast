package com.copperleaf.ballast.examples.injector

import androidx.fragment.app.Fragment
import androidx.lifecycle.SavedStateHandle
import com.copperleaf.ballast.BallastViewModelConfiguration
import com.copperleaf.ballast.ExperimentalBallastApi
import com.copperleaf.ballast.build
import com.copperleaf.ballast.core.AndroidBallastLogger
import com.copperleaf.ballast.core.KillSwitch
import com.copperleaf.ballast.core.LoggingInterceptor
import com.copperleaf.ballast.core.PrintlnLogger
import com.copperleaf.ballast.debugger.BallastDebuggerClientConnection
import com.copperleaf.ballast.debugger.BallastDebuggerInterceptor
import com.copperleaf.ballast.dispatchers
import com.copperleaf.ballast.examples.api.BggApiImpl
import com.copperleaf.ballast.examples.preferences.BallastExamplesPreferencesImpl
import com.copperleaf.ballast.examples.repository.BggRepositoryContract
import com.copperleaf.ballast.examples.repository.BggRepositoryImpl
import com.copperleaf.ballast.examples.repository.BggRepositoryInputHandler
import com.copperleaf.ballast.examples.router.BallastExamples
import com.copperleaf.ballast.examples.router.BallastExamplesRouter
import com.copperleaf.ballast.examples.router.BallastExamplesRouterEventHandler
import com.copperleaf.ballast.examples.ui.MainActivity
import com.copperleaf.ballast.examples.ui.bgg.BggContract
import com.copperleaf.ballast.examples.ui.bgg.BggEventHandler
import com.copperleaf.ballast.examples.ui.bgg.BggInputHandler
import com.copperleaf.ballast.examples.ui.bgg.BggViewModel
import com.copperleaf.ballast.examples.ui.counter.CounterContract
import com.copperleaf.ballast.examples.ui.counter.CounterEventHandler
import com.copperleaf.ballast.examples.ui.counter.CounterInputHandler
import com.copperleaf.ballast.examples.ui.counter.CounterSavedStateAdapter
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
import com.copperleaf.ballast.navigation.routing.RoutingTable
import com.copperleaf.ballast.navigation.routing.fromEnum
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
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.onEach
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalBallastApi::class)
class AndroidInjectorImpl(
    private val applicationScope: CoroutineScope,
) : AndroidInjector {

// Router
// ---------------------------------------------------------------------------------------------------------------------

    private fun newViewModelScope() : CoroutineScope {
        return CoroutineScope(SupervisorJob() + Dispatchers.Main)
    }

    private val router by lazy {
        BallastExamplesRouter(
            config = commonBuilder()
                .withRouter(RoutingTable.fromEnum(BallastExamples.values()), BallastExamples.Home)
                .build(),
            coroutineScope = newViewModelScope(),
        )
    }

    override fun router(): BallastExamplesRouter {
        return router
    }

    override fun routerEventHandler(activity: MainActivity): BallastExamplesRouterEventHandler {
        return BallastExamplesRouterEventHandler(activity)
    }

// Counter
// ---------------------------------------------------------------------------------------------------------------------

    override fun counterViewModel(
        savedStateHandle: SavedStateHandle?,
        syncClientType: DefaultSyncConnection.ClientType?,
        syncAdapter: SyncConnectionAdapter<
                CounterContract.Inputs,
                CounterContract.Events,
                CounterContract.State>?,
    ): CounterViewModel {
        return CounterViewModel(
            config = commonBuilder()
                .apply {
                    if (savedStateHandle != null) {
                        this += BallastSavedStateInterceptor(CounterSavedStateAdapter(savedStateHandle))
                    }
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
            coroutineScope = newViewModelScope(),
        )
    }

    override fun counterEventHandler(fragment: Fragment): CounterEventHandler {
        return CounterEventHandler(fragment, router)
    }

// Scorekeeper
// ---------------------------------------------------------------------------------------------------------------------

    private val preferences = BallastExamplesPreferencesImpl(Settings())

    override fun scorekeeperViewModel(): ScorekeeperViewModel {
        return ScorekeeperViewModel(
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
            coroutineScope = newViewModelScope(),
        )
    }

    override fun scorekeeperEventHandler(fragment: Fragment): ScorekeeperEventHandler {
        return ScorekeeperEventHandler(fragment, router)
    }

// Undo
// ---------------------------------------------------------------------------------------------------------------------

    override fun undoViewModel(
        undoController: UndoController<
            UndoContract.Inputs,
            UndoContract.Events,
            UndoContract.State>
    ): UndoViewModel {
        return UndoViewModel(
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
            coroutineScope = newViewModelScope(),
        )
    }

    override fun undoEventHandler(
        fragment: Fragment,
        undoController: UndoController<
            UndoContract.Inputs,
            UndoContract.Events,
            UndoContract.State>
    ): UndoEventHandler {
        return UndoEventHandler(fragment, router, undoController)
    }

// BGG API Call/Cache
// ---------------------------------------------------------------------------------------------------------------------

    private val eventBus = EventBusImpl()

    private val httpClient = HttpClient(OkHttp) {
        install(Logging) {
            level = LogLevel.BODY
            logger = object : Logger {
                override fun log(message: String) {
                    PrintlnLogger("Ktor").info(message)
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

    override fun bggViewModel(): BggViewModel {
        return BggViewModel(
            config = commonBuilder()
                .withViewModel(
                    initialState = BggContract.State(),
                    inputHandler = BggInputHandler(bggRepository),
                    name = "BGG",
                )
                .build(),
            coroutineScope = newViewModelScope(),
        )
    }

    override fun bggEventHandler(fragment: Fragment): BggEventHandler {
        return BggEventHandler(fragment, router)
    }

// Kitchen Sink
// ---------------------------------------------------------------------------------------------------------------------

    override fun kitchenSinkViewModel(
        inputStrategy: InputStrategySelection,
    ): KitchenSinkViewModel {
        val killSwitch = KillSwitch<
                KitchenSinkContract.Inputs,
                KitchenSinkContract.Events,
                KitchenSinkContract.State>(5.seconds)
        return KitchenSinkViewModel(
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
            coroutineScope = newViewModelScope(),
        )
    }

    override fun kitchenSinkEventHandler(fragment: Fragment): KitchenSinkEventHandler {
        return KitchenSinkEventHandler(fragment, router)
    }

// Helpers
// ---------------------------------------------------------------------------------------------------------------------

    private val debuggerConnection by lazy {
        BallastDebuggerClientConnection(
            engineFactory = OkHttp,
            applicationCoroutineScope = applicationScope,
            host = "10.0.2.2",
        ).also {
            it.connect()
        }
    }

    private fun commonBuilder(): BallastViewModelConfiguration.Builder {
        return BallastViewModelConfiguration.Builder()
            .apply {
                this += LoggingInterceptor()
                this += BallastDebuggerInterceptor(debuggerConnection)
                logger = ::AndroidBallastLogger
            }
            .dispatchers(
                inputsDispatcher = Dispatchers.Main,
                eventsDispatcher = Dispatchers.Main,
                sideJobsDispatcher = Dispatchers.Default,
                interceptorDispatcher = Dispatchers.Default,
            )
    }
}
