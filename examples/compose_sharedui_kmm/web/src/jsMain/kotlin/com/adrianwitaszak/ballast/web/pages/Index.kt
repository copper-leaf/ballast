package com.adrianwitaszak.ballast.web.pages

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import com.ballast.shoppe.feature.router.RouterScreen
import com.ballast.shoppe.feature.router.RouterViewModel
import com.copperleaf.ballast.navigation.browser.BrowserHashNavigationInterceptor
import com.copperleaf.ballast.navigation.routing.Backstack
import com.copperleaf.ballast.navigation.routing.RouterContract
import com.copperleaf.ballast.navigation.routing.build
import com.copperleaf.ballast.navigation.routing.directions
import com.copperleaf.ballast.navigation.routing.renderCurrentDestination
import com.copperleaf.ballast.navigation.vm.Router
import com.copperleaf.ballast.plusAssign
import com.varabyte.kobweb.core.Page

@Page("/")
@Composable
fun HomePage() {
    val coroutineScope = rememberCoroutineScope()
    val router: Router<RouterScreen> =
        remember(coroutineScope) {
            RouterViewModel(
                viewModelScope = coroutineScope,
                initialRoute = null,
            ) { this += BrowserHashNavigationInterceptor<RouterScreen>(RouterScreen.Home) }
        }
    val routerState: Backstack<RouterScreen> by router.observeStates().collectAsState()

    routerState.renderCurrentDestination(
        route = { routerScreen: RouterScreen ->
            when (routerScreen) {
                RouterScreen.Home -> Home(
                    goToCounter = {
                        router.trySend(RouterContract.Inputs.GoToDestination(RouterScreen.Counter.directions().build()))
                    }
                )

                RouterScreen.Counter -> Counter(
                    goBack = { router.trySend(RouterContract.Inputs.GoBack()) },
                )
            }
        },
        notFound = { },
    )
}
