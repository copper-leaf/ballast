package com.copperleaf.ballast.examples

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.ListItem
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.singleWindowApplication
import com.copperleaf.ballast.examples.injector.ComposeDesktopInjector
import com.copperleaf.ballast.examples.injector.ComposeDesktopInjectorImpl
import com.copperleaf.ballast.examples.router.BallastExamples
import com.copperleaf.ballast.examples.ui.bgg.BggUi
import com.copperleaf.ballast.examples.ui.counter.CounterUi
import com.copperleaf.ballast.examples.ui.kitchensink.InputStrategySelection
import com.copperleaf.ballast.examples.ui.kitchensink.KitchenSinkUi
import com.copperleaf.ballast.examples.ui.scorekeeper.ScorekeeperUi
import com.copperleaf.ballast.examples.ui.storefront.StorefrontUi
import com.copperleaf.ballast.examples.ui.sync.SyncUi
import com.copperleaf.ballast.examples.ui.undo.UndoUi
import com.copperleaf.ballast.navigation.routing.RouterContract
import com.copperleaf.ballast.navigation.routing.build
import com.copperleaf.ballast.navigation.routing.currentDestinationOrNull
import com.copperleaf.ballast.navigation.routing.currentRouteOrNull
import com.copperleaf.ballast.navigation.routing.directions
import com.copperleaf.ballast.navigation.routing.optionalStringQuery
import org.jetbrains.compose.splitpane.ExperimentalSplitPaneApi
import org.jetbrains.compose.splitpane.HorizontalSplitPane
import org.jetbrains.compose.splitpane.rememberSplitPaneState

@OptIn(ExperimentalAnimationApi::class, ExperimentalSplitPaneApi::class)
fun main() = singleWindowApplication(title = "Ballast Examples") {
    val applicationScope = rememberCoroutineScope()
    val injector: ComposeDesktopInjector = remember(applicationScope) { ComposeDesktopInjectorImpl(applicationScope) }

    val routerUndoController = remember(injector) { injector.routerUndoController() }
    val isUndoAvailable by routerUndoController.isUndoAvailable.collectAsState(false)
    val isRedoAvailable by routerUndoController.isRedoAvailable.collectAsState(false)

    val router = remember(injector) { injector.router() }
    val routerState by router.observeStates().collectAsState()
    val currentRoute = routerState.currentRouteOrNull
    val currentMainDestination = routerState.currentDestinationOrNull

    HorizontalSplitPane(
        splitPaneState = rememberSplitPaneState(initialPositionPercentage = 0.30f)
    ) {
        first(minSize = 48.dp) {
            Surface(Modifier.fillMaxSize(), elevation = 4.dp) {
                Column(Modifier.fillMaxSize()) {
                    TopAppBar {
                        IconButton(
                            onClick = { routerUndoController.undo() },
                            enabled = isUndoAvailable,
                        ) {
                            Icon(Icons.Default.ArrowBack, "Navigate Back")
                        }
                        IconButton(
                            onClick = { routerUndoController.redo() },
                            enabled = isRedoAvailable,
                        ) {
                            Icon(Icons.Default.ArrowForward, "Navigate Forward")
                        }
                    }

                    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {

                        ListItem(
                            modifier = Modifier
                                .routeLink(
                                    BallastExamples.Counter,
                                    currentRoute,
                                    router::trySend,
                                )
                        ) { Text("Counter") }
                        ListItem(
                            modifier = Modifier
                                .routeLink(
                                    BallastExamples.Scorekeeper,
                                    currentRoute,
                                    router::trySend,
                                )
                        ) { Text("Scorekeeper") }
                        ListItem(
                            modifier = Modifier
                                .routeLink(
                                    BallastExamples.Sync,
                                    currentRoute,
                                    router::trySend,
                                )
                        ) { Text("Sync") }
                        ListItem(
                            modifier = Modifier
                                .routeLink(
                                    BallastExamples.Undo,
                                    currentRoute,
                                    router::trySend,
                                )
                        ) { Text("Undo/Redo") }
                        ListItem(
                            modifier = Modifier
                                .routeLink(
                                    BallastExamples.ApiCall,
                                    currentRoute,
                                    router::trySend,
                                )
                        ) { Text("API Call & Cache") }
                        ListItem(
                            modifier = Modifier
                                .routeLink(
                                    BallastExamples.KitchenSink,
                                    currentRoute,
                                    router::trySend,
                                )
                        ) { Text("Kitchen Sink") }
                        ListItem(
                            modifier = Modifier
                                .routeLink(
                                    BallastExamples.Storefront,
                                    currentRoute,
                                    router::trySend,
                                )
                        ) { Text("Storefront") }
                    }
                }
            }
        }
        second(minSize = 48.dp) {
            Box(Modifier.fillMaxSize()) {
                AnimatedContent(currentMainDestination, Modifier.fillMaxSize()) {
                    Column {
                        when (it?.originalRoute) {
                            BallastExamples.Counter -> {
                                CounterUi.Content(injector)
                            }

                            BallastExamples.Scorekeeper -> {
                                ScorekeeperUi.Content(injector)
                            }

                            BallastExamples.Sync -> {
                                SyncUi.Content(injector)
                            }

                            BallastExamples.Undo -> {
                                UndoUi.Content(injector)
                            }

                            BallastExamples.ApiCall -> {
                                BggUi.Content(injector)
                            }

                            BallastExamples.KitchenSink -> {
                                val inputStrategy by it.optionalStringQuery()
                                val inputStrategySelection = InputStrategySelection.valueOf(
                                    inputStrategy ?: "Lifo"
                                )
                                KitchenSinkUi.Content(injector, inputStrategySelection)
                            }

                            BallastExamples.Storefront -> {
                                StorefrontUi.Content(injector)
                            }

                            null -> {}
                        }
                    }
                }
            }
        }
    }
}

private fun Modifier.routeLink(
    targetRoute: BallastExamples,
    currentRoute: BallastExamples?,
    postInput: (RouterContract.Inputs<BallastExamples>) -> Unit,
): Modifier = composed {
    Modifier
        .clickable {
            postInput(
                RouterContract.Inputs.GoToDestination(
                    targetRoute.directions().build(),
                )
            )
        }
        .then(
            if (currentRoute == targetRoute) {
                Modifier.background(MaterialTheme.colors.primary.copy(alpha = 0.1f))
            } else {
                Modifier
            }
        )
}
