package com.copperleaf.ballast.debugger.idea.ui.debugger.injector

import com.copperleaf.ballast.build
import com.copperleaf.ballast.core.BasicViewModel
import com.copperleaf.ballast.core.BootstrapInterceptor
import com.copperleaf.ballast.core.FifoInputStrategy
import com.copperleaf.ballast.core.LoggingInterceptor
import com.copperleaf.ballast.debugger.idea.BallastIntellijPluginInjector
import com.copperleaf.ballast.debugger.idea.ui.debugger.DebuggerUiContract
import com.copperleaf.ballast.debugger.idea.ui.debugger.DebuggerUiInputHandler
import com.copperleaf.ballast.debugger.idea.ui.debugger.DebuggerUiViewModel
import com.copperleaf.ballast.debugger.idea.ui.debugger.router.DebuggerRoute
import com.copperleaf.ballast.debugger.idea.ui.debugger.router.DebuggerRouter
import com.copperleaf.ballast.debugger.idea.ui.debugger.widgets.getRouteForSelectedViewModel
import com.copperleaf.ballast.debugger.server.vm.DebuggerServerContract
import com.copperleaf.ballast.debugger.server.vm.DebuggerServerInputHandler
import com.copperleaf.ballast.debugger.server.vm.DebuggerServerViewModel
import com.copperleaf.ballast.eventHandler
import com.copperleaf.ballast.navigation.routing.RouterContract
import com.copperleaf.ballast.navigation.routing.RoutingTable
import com.copperleaf.ballast.navigation.routing.fromEnum
import com.copperleaf.ballast.navigation.vm.BasicRouter
import com.copperleaf.ballast.navigation.vm.withRouter
import com.copperleaf.ballast.plusAssign
import com.copperleaf.ballast.withViewModel
import kotlinx.coroutines.CoroutineScope

class DebuggerToolWindowInjectorImpl(
    private val pluginInjector: BallastIntellijPluginInjector,
    private val toolWindowCoroutineScope: CoroutineScope,
) : DebuggerToolWindowInjector {
    private val settingsSnapshot = pluginInjector.settings.snapshot()

    override val debuggerRouter: DebuggerRouter = BasicRouter(
        coroutineScope = toolWindowCoroutineScope,
        config = pluginInjector
            .commonViewModelBuilder()
            .withRouter(
                routingTable = RoutingTable.fromEnum(DebuggerRoute.values()),
                initialRoute = DebuggerRoute.Connection
            )
            .apply {
                this += LoggingInterceptor()
            }
            .build(),
        eventHandler = eventHandler { event ->
            when (event) {
                is RouterContract.Events.BackstackChanged -> {
//                    val currentDestination = event.backstack.currentDestinationOrNull

//                    if (currentDestination != null) {
//                        val route = currentDestination.originalRoute
//                        val viewModelName by currentDestination.optionalStringPath()

//                        pluginInjector
//                            .settings
//                            .edit()
//                            .also {
//                                it.lastRoute = route
//                                if (viewModelName != null) {
//                                    it.lastViewModelName = viewModelName!!
//                                }
//                            }
//                            .save()
//                    }
                }

                is RouterContract.Events.BackstackEmptied -> {}
                is RouterContract.Events.NoChange -> {}
            }
        },
    )

    override val debuggerServerViewModel: DebuggerServerViewModel = BasicViewModel(
        coroutineScope = toolWindowCoroutineScope,
        config = pluginInjector
            .commonViewModelBuilder()
            .apply {
                inputStrategy = FifoInputStrategy()
                this += BootstrapInterceptor {
                    DebuggerServerContract.Inputs.StartServer(settingsSnapshot)
                }
            }
            .withViewModel(
                initialState = DebuggerServerContract.State(),
                inputHandler = DebuggerServerInputHandler(),
                name = "Debugger",
            )
            .build(),
        eventHandler = eventHandler {
            when (it) {
                is DebuggerServerContract.Events.ConnectionEstablished -> {
                    if (settingsSnapshot.autoselectDebuggerConnections) {
                        val latestRoute = pluginInjector.settings.lastRoute
                        val latestViewModelName = pluginInjector.settings.lastViewModelName
                        println("Autoselecting route:")
                        println("    connectionId: ${it.connectionId}")
                        println("    latestRoute: $latestRoute")
                        println("    latestViewModelName: $latestViewModelName")

                        val route = if (latestViewModelName.isNotBlank()) {
                            getRouteForSelectedViewModel(
                                pluginInjector.settings.lastRoute,
                                it.connectionId,
                                latestViewModelName
                            )
                        } else {
                            getRouteForSelectedViewModel(
                                pluginInjector.settings.lastRoute,
                                it.connectionId,
                                null,
                            )
                        }

                        debuggerUiViewModel.send(
                            DebuggerUiContract.Inputs.Navigate(route)
                        )
                    }
                }
            }
        },
    )

    override val debuggerUiViewModel: DebuggerUiViewModel = BasicViewModel(
        coroutineScope = toolWindowCoroutineScope,
        config = pluginInjector
            .commonViewModelBuilder()
            .apply {
                this += BootstrapInterceptor {
                    DebuggerUiContract.Inputs.Initialize
                }
            }
            .withViewModel(
                initialState = DebuggerUiContract.State(settingsSnapshot),
                inputHandler = DebuggerUiInputHandler(
                    debuggerServerViewModel.observeStates(),
                    debuggerRouter.observeStates(),
                ),
                name = "DebuggerUi",
            )
            .build(),
        eventHandler = eventHandler {
            when (it) {
                is DebuggerUiContract.Events.SendCommandToRouter -> {
                    debuggerRouter.trySend(it.input)
                    Unit
                }

                is DebuggerUiContract.Events.SendCommandToDebuggerServer -> {
                    debuggerServerViewModel.trySend(it.input)
                    Unit
                }
            }
        },
    )
}
