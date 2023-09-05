package com.copperleaf.ballast.examples

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import com.copperleaf.ballast.examples.injector.ComposeWebInjector
import com.copperleaf.ballast.examples.injector.ComposeWebInjectorImpl
import com.copperleaf.ballast.examples.router.BallastExamples
import com.copperleaf.ballast.examples.ui.bgg.BggWebUi
import com.copperleaf.ballast.examples.ui.counter.CounterWebUi
import com.copperleaf.ballast.examples.ui.kitchensink.InputStrategySelection
import com.copperleaf.ballast.examples.ui.kitchensink.KitchenSinkWebUi
import com.copperleaf.ballast.examples.ui.scorekeeper.ScorekeeperWebUi
import com.copperleaf.ballast.examples.ui.sync.SyncWebUi
import com.copperleaf.ballast.examples.ui.undo.UndoWebUi
import com.copperleaf.ballast.examples.ui.util.bulma.BulmaPanel
import com.copperleaf.ballast.examples.ui.util.bulma.NavLink
import com.copperleaf.ballast.navigation.routing.Destination
import com.copperleaf.ballast.navigation.routing.currentDestinationOrNull
import com.copperleaf.ballast.navigation.routing.currentRouteOrNull
import com.copperleaf.ballast.navigation.routing.optionalStringQuery
import com.copperleaf.ballast.navigation.vm.Router
import kotlinx.browser.document
import org.jetbrains.compose.web.dom.Text
import org.jetbrains.compose.web.renderComposable

fun main() {
    document.getElementById("examples_navigation")?.let { renderComposable(it) { MainApplication(null, useBrowserHashes = true) } }
    document.getElementById("example_counter")?.let { renderComposable(it) { MainApplication(BallastExamples.Counter) } }
    document.getElementById("example_scorekeeper")?.let { renderComposable(it) { MainApplication(BallastExamples.Scorekeeper) } }
    document.getElementById("example_sync")?.let { renderComposable(it) { MainApplication(BallastExamples.Sync) } }
    document.getElementById("example_undo")?.let { renderComposable(it) { MainApplication(BallastExamples.Undo) } }
    document.getElementById("example_bgg")?.let { renderComposable(it) { MainApplication(BallastExamples.ApiCall) } }
    document.getElementById("example_kitchen_sink")?.let { renderComposable(it) { MainApplication(BallastExamples.KitchenSink) } }
}

@Composable
fun MainApplication(forcedDestination: BallastExamples?, useBrowserHashes: Boolean = false) {
    val applicationScope = rememberCoroutineScope()
    val injector: ComposeWebInjector = remember(applicationScope) {
        ComposeWebInjectorImpl(
            applicationScope,
            useBrowserHashes,
            forcedDestination ?: BallastExamples.Counter
        )
    }

    val router = remember(injector) { injector.router() }
    val routerState by router.observeStates().collectAsState()
    val currentDestination = routerState.currentDestinationOrNull
    val currentRoute = routerState.currentRouteOrNull

    NavigationContent(
        router = router,
        destination = currentDestination,
        route = currentRoute,
        injector = injector,
        showTabs = forcedDestination == null,
    )
}

@Composable
fun NavigationContent(
    router: Router<BallastExamples>,
    destination: Destination.Match<BallastExamples>?,
    route: BallastExamples?,
    injector: ComposeWebInjector,
    showTabs: Boolean,
) {
    BulmaPanel(
        headingStart = {
            Text("Ballast Examples")
        },
        tabs = if (showTabs) {
            {
                router.NavLink(BallastExamples.Counter) {
                    Text("Counter")
                }
                router.NavLink(BallastExamples.Scorekeeper) {
                    Text("Scorekeeper")
                }
                router.NavLink(BallastExamples.Sync) {
                    Text("Sync")
                }
                router.NavLink(BallastExamples.Undo) {
                    Text("Undo/Redo")
                }
                router.NavLink(BallastExamples.ApiCall) {
                    Text("API Call & Cache")
                }
                router.NavLink(BallastExamples.KitchenSink) {
                    Text("Kitchen Sink")
                }
            }
        } else {
            null
        }
    ) {
        DestinationContent(destination, route, injector)
    }
}

@Composable
fun DestinationContent(
    destination: Destination.Match<BallastExamples>?,
    route: BallastExamples?,
    injector: ComposeWebInjector,
) {
    when (route) {
        BallastExamples.Counter -> {
            CounterWebUi.Content(injector)
        }

        BallastExamples.Scorekeeper -> {
            ScorekeeperWebUi.Content(injector)
        }

        BallastExamples.Sync -> {
            SyncWebUi.Content(injector)
        }

        BallastExamples.Undo -> {
            UndoWebUi.Content(injector)
        }

        BallastExamples.ApiCall -> {
            BggWebUi.Content(injector)
        }

        BallastExamples.KitchenSink -> {
            val inputStrategy by destination!!.optionalStringQuery()
            val inputStrategySelection = InputStrategySelection.valueOf(
                inputStrategy ?: "Lifo"
            )
            KitchenSinkWebUi.Content(injector, inputStrategySelection)
        }

        null -> {}
    }
}
