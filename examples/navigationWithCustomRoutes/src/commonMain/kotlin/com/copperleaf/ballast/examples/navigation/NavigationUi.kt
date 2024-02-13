package com.copperleaf.ballast.examples.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.staticCompositionLocalOf
import com.copperleaf.ballast.navigation.routing.Backstack
import com.copperleaf.ballast.navigation.routing.renderCurrentDestination
import com.copperleaf.ballast.navigation.vm.Router

internal val LocalRouter = staticCompositionLocalOf<Router<AppScreenRoute>> { error("LocalRouter not provided") }

@ExperimentalMaterial3Api
object NavigationUi {

    @Composable
    fun Content() {
        val applicationScope = rememberCoroutineScope()
        val router: Router<AppScreenRoute> = remember(applicationScope) { createRouter(applicationScope) }

        val routerState: Backstack<AppScreenRoute> by router.observeStates().collectAsState()

        CompositionLocalProvider(LocalRouter provides router) {
            Column {
                routerState.renderCurrentDestination(
                    route = { appScreen: AppScreenRoute ->
                        matchRoute<AppScreen>(appScreen)?.let {
                            it.Content()
                        }
                    },
                    notFound = { },
                )

                Divider()
                Text("Backstack")

                routerState
                    .withIndex()
                    .reversed()
                    .forEach { (index, destination) ->
                        Text("- [$index] ${destination.originalDestinationUrl}")
                    }
            }
        }
    }
}
