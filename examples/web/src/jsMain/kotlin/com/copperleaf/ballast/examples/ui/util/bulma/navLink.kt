package com.copperleaf.ballast.examples.ui.util.bulma

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.copperleaf.ballast.examples.router.BallastExamples
import com.copperleaf.ballast.navigation.routing.RouterContract
import com.copperleaf.ballast.navigation.routing.build
import com.copperleaf.ballast.navigation.routing.currentRouteOrNull
import com.copperleaf.ballast.navigation.routing.directions
import com.copperleaf.ballast.navigation.vm.Router
import org.jetbrains.compose.web.dom.A
import org.jetbrains.compose.web.dom.AttrBuilderContext
import org.w3c.dom.HTMLAnchorElement

@Composable
fun Router<BallastExamples>.NavLink(
    route: BallastExamples,
    attrs: AttrBuilderContext<HTMLAnchorElement>? = null,
    onClicked: () -> Unit = { },
    content: @Composable () -> Unit
) {
    val router = this

    val routerState by router.observeStates().collectAsState()
    val currentRoute = routerState.currentRouteOrNull
    val url = route.directions().build()

    A(
        href = "#$url",
        attrs = {
            onClick {
                if (it.ctrlKey || it.metaKey) {
                    // let it propagate normally, don't handle it with the router
                } else {
                    it.preventDefault()
                    it.stopPropagation()
                    router.trySend(RouterContract.Inputs.GoToDestination(url))
                }
                onClicked()
            }
            if (route == currentRoute) {
                classes("is-active")
            }
            attrs?.invoke(this)
        }
    ) {
        content()
    }
}
