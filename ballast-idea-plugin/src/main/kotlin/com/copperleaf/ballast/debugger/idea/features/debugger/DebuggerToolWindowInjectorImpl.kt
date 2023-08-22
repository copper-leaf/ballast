package com.copperleaf.ballast.debugger.idea.features.debugger

import com.copperleaf.ballast.build
import com.copperleaf.ballast.core.BasicViewModel
import com.copperleaf.ballast.debugger.idea.BallastIntellijPluginInjector
import com.copperleaf.ballast.debugger.idea.features.debugger.injector.DebuggerToolWindowInjector
import com.copperleaf.ballast.debugger.idea.features.debugger.router.DebuggerRoute
import com.copperleaf.ballast.debugger.idea.features.debugger.router.DebuggerRouter
import com.copperleaf.ballast.debugger.idea.features.debugger.router.RouterEventHandler
import com.copperleaf.ballast.debugger.idea.features.debugger.server.DebuggerServerEventHandler
import com.copperleaf.ballast.debugger.idea.features.debugger.vm.DebuggerUiContract
import com.copperleaf.ballast.debugger.idea.features.debugger.vm.DebuggerUiInputHandler
import com.copperleaf.ballast.debugger.idea.features.debugger.vm.DebuggerUiViewModel
import com.copperleaf.ballast.debugger.server.vm.DebuggerServerContract
import com.copperleaf.ballast.debugger.server.vm.DebuggerServerInputHandler
import com.copperleaf.ballast.debugger.server.vm.DebuggerServerViewModel
import com.copperleaf.ballast.navigation.routing.RoutingTable
import com.copperleaf.ballast.navigation.routing.fromEnum
import com.copperleaf.ballast.navigation.vm.BasicRouter
import com.copperleaf.ballast.navigation.vm.withRouter
import com.copperleaf.ballast.withViewModel
import kotlinx.coroutines.CoroutineScope

class DebuggerToolWindowInjectorImpl(
    private val pluginInjector: BallastIntellijPluginInjector,
    private val toolWindowCoroutineScope: CoroutineScope,
) : DebuggerToolWindowInjector {
    private val project = pluginInjector.project

    override val debuggerRouter: DebuggerRouter = BasicRouter(
        coroutineScope = toolWindowCoroutineScope,
        config = pluginInjector
            .commonViewModelBuilder(loggingEnabled = false)
            .withRouter(
                routingTable = RoutingTable.fromEnum(DebuggerRoute.values()),
                initialRoute = DebuggerRoute.Home,
            )
            .build(),
        eventHandler = RouterEventHandler(),
    )

    override val debuggerServerViewModel: DebuggerServerViewModel = BasicViewModel(
        coroutineScope = toolWindowCoroutineScope,
        config = pluginInjector
            .commonViewModelBuilder(loggingEnabled = true)
            .withViewModel(
                initialState = DebuggerServerContract.State(),
                inputHandler = DebuggerServerInputHandler(),
                name = "Debugger Server",
            )
            .build(),
        eventHandler = DebuggerServerEventHandler(
            getDebuggerUiViewModelLazy = { debuggerUiViewModel }
        ),
    )

    override val debuggerUiViewModel: DebuggerUiViewModel = BasicViewModel(
        coroutineScope = toolWindowCoroutineScope,
        config = pluginInjector
            .commonViewModelBuilder(loggingEnabled = false) {
                DebuggerUiContract.Inputs.Initialize
            }
            .withViewModel(
                initialState = DebuggerUiContract.State(),
                inputHandler = DebuggerUiInputHandler(
                    debuggerRouter,
                    debuggerServerViewModel,
                    pluginInjector.debuggerUseCase,
                ),
                name = "Debugger Ui",
            )
            .build(),
        eventHandler = DebuggerUiEventHandler(),
    )
}
