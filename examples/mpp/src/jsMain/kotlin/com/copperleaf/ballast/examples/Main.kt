package com.copperleaf.ballast.examples

import androidx.compose.runtime.remember
import com.copperleaf.ballast.examples.bgg.BggComponent
import com.copperleaf.ballast.examples.counter.CounterComponent
import com.copperleaf.ballast.examples.kitchensink.KitchenSinkComponent
import com.copperleaf.ballast.examples.scorekeeper.ScorekeeperComponent
import com.copperleaf.ballast.examples.util.Component
import com.copperleaf.ballast.examples.util.ComposeWebInjector
import com.copperleaf.ballast.examples.util.ComposeWebInjectorImpl
import kotlinx.browser.document
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.jetbrains.compose.web.renderComposable

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

