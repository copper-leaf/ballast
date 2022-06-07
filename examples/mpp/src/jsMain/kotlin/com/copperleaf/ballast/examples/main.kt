package com.copperleaf.ballast.examples

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import com.copperleaf.ballast.examples.bgg.BggWebUi
import com.copperleaf.ballast.examples.counter.CounterWebUi
import com.copperleaf.ballast.examples.kitchensink.controller.KitchenSinkControllerWebUi
import com.copperleaf.ballast.examples.scorekeeper.ScorekeeperWebUi
import com.copperleaf.ballast.examples.util.ComposeWebInjector
import com.copperleaf.ballast.examples.util.ComposeWebInjectorImpl
import com.copperleaf.ballast.examples.util.LocalInjector
import kotlinx.browser.document
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.jetbrains.compose.web.renderComposable

fun main() {
    val components: Map<String, @Composable () -> Unit> = mapOf(
        "example_counter" to { CounterWebUi.WebContent() },
        "example_bgg" to { BggWebUi.WebContent() },
        "example_kitchen_sink" to { KitchenSinkControllerWebUi.WebContent() },
        "example_scorekeeper" to { ScorekeeperWebUi.WebContent() },
        "all_examples" to { },
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
