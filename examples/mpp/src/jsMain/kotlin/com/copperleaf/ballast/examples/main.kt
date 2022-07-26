package com.copperleaf.ballast.examples

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import com.copperleaf.ballast.examples.bgg.BggWebUi
import com.copperleaf.ballast.examples.counter.CounterWebUi
import com.copperleaf.ballast.examples.kitchensink.controller.KitchenSinkControllerWebUi
import com.copperleaf.ballast.examples.mainlist.MainWebUi
import com.copperleaf.ballast.examples.navigation.Routes
import com.copperleaf.ballast.examples.scorekeeper.ScorekeeperWebUi
import com.copperleaf.ballast.examples.util.ComposeWebInjector
import com.copperleaf.ballast.examples.util.ComposeWebInjectorImpl
import com.copperleaf.ballast.examples.util.LocalInjector
import com.copperleaf.ballast.navigation.routing.currentDestination
import kotlinx.browser.document
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.renderComposable

fun main() {
    val components: Map<String, @Composable () -> Unit> = mapOf(
        "example_counter" to { CounterWebUi.WebContent(false) },
        "example_bgg" to { BggWebUi.WebContent() },
        "example_kitchen_sink" to { KitchenSinkControllerWebUi.WebContent() },
        "example_scorekeeper" to { ScorekeeperWebUi.WebContent() },
        "example_sync" to { CounterWebUi.WebContent(true) },
        "example_navigation" to {
            val injector = LocalInjector.current
            val router = remember(injector) { injector.routerViewModel() }

            Div {
                Div(attrs = { classes("column", "is-centered")}) {
                    val mainCoroutineScope = rememberCoroutineScope()
                    val mainVm = remember(injector, mainCoroutineScope) { injector.mainViewModel(mainCoroutineScope) }
                    val mainUiState by mainVm.observeStates().collectAsState()

                    MainWebUi.WebContent(
                        uiState = mainUiState,
                    ) { mainVm.trySend(it) }
                }
                Div(attrs = { classes("column", "is-centered")}) {
                    val currentScreen by router.observeStates().collectAsState()
                    when (currentScreen.currentDestination?.originalRoute) {
                        Routes.Main -> {
                            // ignore, main component is in the list pane instead of its own screen
                        }
                        Routes.Counter -> {
                            CounterWebUi.WebContent(false)
                        }
                        Routes.BoardGameGeek -> {
                            BggWebUi.WebContent()
                        }
                        Routes.Scorekeeper -> {
                            ScorekeeperWebUi.WebContent()
                        }
                        Routes.KitchenSink -> {
                            KitchenSinkControllerWebUi.WebContent()
                        }
                        else -> {}
                    }
                }
            }
        },
    )

    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    val injector: ComposeWebInjector = ComposeWebInjectorImpl(applicationScope)

    components.forEach { (componentId, component) ->
        val mountingPoint = document.getElementById(componentId)
        if (mountingPoint != null) {
            renderComposable(root = mountingPoint) {
                CompositionLocalProvider(LocalInjector provides injector) {
                    component()
                }
            }
        }
    }
}
