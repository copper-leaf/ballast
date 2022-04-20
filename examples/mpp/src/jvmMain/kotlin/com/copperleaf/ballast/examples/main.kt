package com.copperleaf.ballast.examples

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ListItem
import androidx.compose.material.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.singleWindowApplication
import com.copperleaf.ballast.examples.bgg.BggComponent
import com.copperleaf.ballast.examples.counter.CounterComponent
import com.copperleaf.ballast.examples.kitchensink.KitchenSinkComponent
import com.copperleaf.ballast.examples.scorekeeper.ScorekeeperComponent
import com.copperleaf.ballast.examples.util.Component
import com.copperleaf.ballast.examples.util.ComposeDesktopInjector
import com.copperleaf.ballast.examples.util.ComposeDesktopInjectorImpl
import org.jetbrains.compose.splitpane.ExperimentalSplitPaneApi
import org.jetbrains.compose.splitpane.HorizontalSplitPane
import org.jetbrains.compose.splitpane.rememberSplitPaneState

@OptIn(ExperimentalAnimationApi::class, ExperimentalSplitPaneApi::class)
fun main() = singleWindowApplication {

    val components: Map<String, (ComposeDesktopInjector) -> Component> = remember {
        mapOf(
            "Counter" to { CounterComponent(it) },
            "BoardGameGeek API" to { BggComponent(it) },
            "Kitchen Sink" to { KitchenSinkComponent(it) },
            "Scorekeeper" to { ScorekeeperComponent(it) },
        )
    }

    val applicationScope = rememberCoroutineScope()
    val injector: ComposeDesktopInjector = remember(applicationScope) { ComposeDesktopInjectorImpl(applicationScope) }
    var focusedState: Component by remember { mutableStateOf(CounterComponent(injector)) }
//    var focusedState: Component = CounterComponent(injector)

    HorizontalSplitPane(
        splitPaneState = rememberSplitPaneState(initialPositionPercentage = 0.30f)
    ) {
        first(minSize = 48.dp) {
            Column(Modifier.fillMaxSize()) {
                LazyColumn {
                    items(components.entries.toList()) { (name, factory) ->
                        ListItem(
                            modifier = Modifier.clickable {
                                focusedState = factory(injector)
                            }
                        ) {
                            Text(name)
                        }
                    }
                }
            }
        }
        second(minSize = 48.dp) {
            Box {
                AnimatedContent(focusedState) { component ->
                    component.Content()
                }
            }
        }
    }
}
