package com.copperleaf.ballast.examples.web.internal

import com.copperleaf.ballast.BallastViewModelConfiguration
import com.copperleaf.ballast.InputStrategy
import com.copperleaf.ballast.core.LoggingInterceptor
import com.copperleaf.ballast.core.JsConsoleBallastLogger
import com.copperleaf.ballast.debugger.BallastDebuggerClientConnection
import com.copperleaf.ballast.debugger.BallastDebuggerInterceptor
import com.copperleaf.ballast.examples.bgg.repository.BggRepositoryImpl
import com.copperleaf.ballast.examples.bgg.ui.BggViewModel
import com.copperleaf.ballast.examples.counter.CounterViewModel
import com.copperleaf.ballast.examples.kitchensink.KitchenSinkViewModel
import com.copperleaf.ballast.examples.scorekeeper.ScorekeeperViewModel
import com.copperleaf.ballast.examples.scorekeeper.prefs.ScoreKeeperPrefs
import com.copperleaf.ballast.plusAssign
import com.copperleaf.ballast.repository.bus.EventBusImpl
import io.ktor.client.HttpClient
import io.ktor.client.engine.js.Js
import kotlinx.browser.window
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class, ExperimentalCoroutinesApi::class)
interface ComposeWebInjector {

    fun kitchenSinkViewModel(
        coroutineScope: CoroutineScope,
        inputStrategy: InputStrategy,
    ): KitchenSinkViewModel

    fun counterViewModel(
        coroutineScope: CoroutineScope,
    ): CounterViewModel

    fun bggViewModel(
        coroutineScope: CoroutineScope,
    ): BggViewModel

    fun scorekeeperViewModel(
        coroutineScope: CoroutineScope,
    ): ScorekeeperViewModel
}

@OptIn(ExperimentalTime::class, ExperimentalCoroutinesApi::class)
class ComposeWebInjectorImpl(
    private val applicationScope: CoroutineScope,
) : ComposeWebInjector {
    private val httpClient = HttpClient(Js)
    private val debuggerConnection by lazy {
        BallastDebuggerClientConnection(Js, applicationScope).also { it.connect() }
    }
    private val eventBus = EventBusImpl()
    private val bggApi = BggApiImpl(httpClient)
    private val bggRepository by lazy {
        BggRepositoryImpl(
            coroutineScope = applicationScope,
            eventBus = eventBus,
            configBuilder = commonBuilder(),
            api = bggApi
        )
    }
    private val prefs by lazy {
        object : ScoreKeeperPrefs {
            private val buttonValuesKey = "ScoreKeeper.buttonValues"
            private val buttonValuesSerializer = ListSerializer(Int.serializer())
            override var buttonValues: List<Int>
                get() {
                    val buttonValuesJsonString = window.localStorage.getItem(buttonValuesKey) ?: "[]"
                    return Json.decodeFromString(buttonValuesSerializer, buttonValuesJsonString)
                }
                set(value) {
                    val buttonValuesJsonString = Json.encodeToString(buttonValuesSerializer, value)
                    window.localStorage.setItem(buttonValuesKey, buttonValuesJsonString)
                }

            private val scoresheetStateKey = "ScoreKeeper.scoresheetState"
            private val scoresheetStateSerializer = MapSerializer(String.serializer(), Int.serializer())
            override var scoresheetState: Map<String, Int>
                get() {
                    val buttonValuesJsonString = window.localStorage.getItem(scoresheetStateKey) ?: "{}"
                    return Json.decodeFromString(scoresheetStateSerializer, buttonValuesJsonString)
                }
                set(value) {
                    val buttonValuesJsonString = Json.encodeToString(scoresheetStateSerializer, value)
                    window.localStorage.setItem(scoresheetStateKey, buttonValuesJsonString)
                }
        }
    }

    override fun kitchenSinkViewModel(
        coroutineScope: CoroutineScope,
        inputStrategy: InputStrategy,
    ): KitchenSinkViewModel {
        return KitchenSinkViewModel(
            viewModelCoroutineScope = coroutineScope,
            configurationBuilder = commonBuilder()
                .apply {
                    this.inputStrategy = inputStrategy
                },
            onWindowClosed = {
                // ignore
            }
        )
    }

    override fun counterViewModel(coroutineScope: CoroutineScope): CounterViewModel {
        return CounterViewModel(
            viewModelCoroutineScope = coroutineScope,
            configurationBuilder = commonBuilder(),
        )
    }

    override fun bggViewModel(coroutineScope: CoroutineScope): BggViewModel {
        return BggViewModel(
            viewModelCoroutineScope = coroutineScope,
            configurationBuilder = commonBuilder(),
            repository = bggRepository,
        )
    }

    override fun scorekeeperViewModel(
        coroutineScope: CoroutineScope,
    ): ScorekeeperViewModel {
        return ScorekeeperViewModel(
            viewModelCoroutineScope = coroutineScope,
            configurationBuilder = commonBuilder(),
            prefs = prefs,
            displayErrorMessage = {
                window.alert(it)
            }
        )
    }

    private fun commonBuilder(): BallastViewModelConfiguration.Builder {
        return BallastViewModelConfiguration.Builder()
            .apply {
                this += LoggingInterceptor()
                this += BallastDebuggerInterceptor(debuggerConnection)
                logger = { JsConsoleBallastLogger() }
            }
    }
}
