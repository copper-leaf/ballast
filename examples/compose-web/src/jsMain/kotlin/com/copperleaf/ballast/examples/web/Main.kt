package com.copperleaf.ballast.examples.web

import androidx.compose.runtime.remember
import com.copperleaf.ballast.examples.web.bgg.BggComponent
import com.copperleaf.ballast.examples.web.counter.CounterComponent
import com.copperleaf.ballast.examples.web.util.Component
import com.copperleaf.ballast.examples.web.util.ComposeWebInjector
import com.copperleaf.ballast.examples.web.util.ComposeWebInjectorImpl
import com.copperleaf.ballast.examples.web.kitchensink.KitchenSinkComponent
import com.copperleaf.ballast.examples.web.scorekeeper.ScorekeeperComponent
import kotlinx.browser.document
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.jetbrains.compose.web.renderComposable
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
fun main() {
    val components: Map<String, (ComposeWebInjector) -> Component> = mapOf(
        "example_counter" to { CounterComponent(it) },
        "example_bgg" to { BggComponent(it) },
        "example_kitchen_sink" to { KitchenSinkComponent(it) },
        "example_scorekeeper" to { ScorekeeperComponent(it) },
    )

    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    val injector: ComposeWebInjector = ComposeWebInjectorImpl(applicationScope)

    components.forEach { (componentId, component) ->
        val mountingPoint = document.getElementById(componentId)
        if (mountingPoint != null) {
            renderComposable(root = mountingPoint) {
                remember(injector) { component(injector) }.Content()
            }
        }
    }
}

