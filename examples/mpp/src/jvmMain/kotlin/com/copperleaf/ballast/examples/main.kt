package com.copperleaf.ballast.examples

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.singleWindowApplication
import com.copperleaf.ballast.examples.bgg.BggDesktopUi
import com.copperleaf.ballast.examples.counter.CounterDesktopUi
import com.copperleaf.ballast.examples.kitchensink.KitchenSinkComponent
import com.copperleaf.ballast.examples.mainlist.MainComposeUi
import com.copperleaf.ballast.examples.navigation.Routes
import com.copperleaf.ballast.examples.scorekeeper.ScorekeeperComponent
import com.copperleaf.ballast.examples.util.ComposeDesktopInjector
import com.copperleaf.ballast.examples.util.ComposeDesktopInjectorImpl
import com.copperleaf.ballast.examples.util.LocalInjector
import com.copperleaf.ballast.navigation.routing.currentDestination
import org.jetbrains.compose.splitpane.ExperimentalSplitPaneApi
import org.jetbrains.compose.splitpane.HorizontalSplitPane
import org.jetbrains.compose.splitpane.rememberSplitPaneState

@OptIn(ExperimentalAnimationApi::class, ExperimentalSplitPaneApi::class)
fun main() = singleWindowApplication {

    val applicationScope = rememberCoroutineScope()
    val injector: ComposeDesktopInjector = remember(applicationScope) { ComposeDesktopInjectorImpl(applicationScope) }

    MaterialTheme {
        CompositionLocalProvider(LocalInjector provides injector) {
            HorizontalSplitPane(
                splitPaneState = rememberSplitPaneState(initialPositionPercentage = 0.30f)
            ) {
                first(minSize = 48.dp) {
                    Scaffold(
                        topBar = {
                            TopAppBar(
                                title = { Text("Ballast Examples") },
                            )
                        },
                        content = {
                            val mainCoroutineScope = rememberCoroutineScope()
                            val mainVm = remember(injector, mainCoroutineScope) { injector.mainViewModel(mainCoroutineScope) }
                            val mainUiState by mainVm.observeStates().collectAsState()

                            MainComposeUi.Content(
                                uiState = mainUiState,
                            ) { mainVm.trySend(it) }
                        }
                    )
                }
                second(minSize = 48.dp) {
                    Box {
                        val router = remember(injector) { injector.routerViewModel() }
                        val currentScreen by router.observeStates().collectAsState()

                        AnimatedContent(currentScreen.currentDestination?.originalRoute) { route ->
                            when (route) {
                                Routes.Main -> {
                                    // ignore, main component is in the list pane instead of its own screen
                                }
                                Routes.Counter -> {
                                    CounterDesktopUi.DesktopContent()
                                }
                                Routes.BoardGameGeek -> {
                                    BggDesktopUi.DesktopContent()
                                }
                                Routes.Scorekeeper -> {
                                    ScorekeeperComponent.DesktopContent()
                                }
                                Routes.KitchenSink -> {
                                    KitchenSinkComponent.DesktopContent()
                                }
                                else -> {}
                            }
                        }
                    }
                }
            }
        }
    }
}
