package com.copperleaf.ballast.debugger.idea.ui.debugger.injector

import com.copperleaf.ballast.build
import com.copperleaf.ballast.core.BasicViewModel
import com.copperleaf.ballast.debugger.idea.BallastIntellijPluginInjector
import com.copperleaf.ballast.debugger.idea.ui.debugger.DebuggerUiContract
import com.copperleaf.ballast.debugger.idea.ui.debugger.DebuggerUiEventHandler
import com.copperleaf.ballast.debugger.idea.ui.debugger.DebuggerUiInputHandler
import com.copperleaf.ballast.debugger.idea.ui.debugger.DebuggerUiViewModel
import com.copperleaf.ballast.debugger.idea.ui.debugger.router.DebuggerRoute
import com.copperleaf.ballast.debugger.idea.ui.debugger.router.DebuggerRouter
import com.copperleaf.ballast.debugger.idea.ui.debugger.router.RouterEventHandler
import com.copperleaf.ballast.debugger.idea.ui.debugger.server.DebuggerServerEventHandler
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
    override val project = pluginInjector.project

    override val debuggerRouter: DebuggerRouter = BasicRouter(
        coroutineScope = toolWindowCoroutineScope,
        config = pluginInjector
            .commonViewModelBuilder(loggingEnabled = false)
            .withRouter(
                routingTable = RoutingTable.fromEnum(DebuggerRoute.values()),
                initialRoute = DebuggerRoute.Connection
            )
            .build(),
        eventHandler = RouterEventHandler(),
    )

    override val debuggerServerViewModel: DebuggerServerViewModel = BasicViewModel(
        coroutineScope = toolWindowCoroutineScope,
        config = pluginInjector
            .commonViewModelBuilder(loggingEnabled = false)
            .withViewModel(
                initialState = DebuggerServerContract.State(),
                inputHandler = DebuggerServerInputHandler(),
                name = "Debugger",
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
                    pluginInjector.repository,
                ),
                name = "DebuggerUi",
            )
            .build(),
        eventHandler = DebuggerUiEventHandler(
            getDebuggerRouterLazy = { debuggerRouter },
            getDebuggerServerViewModelLazy = { debuggerServerViewModel },
        ),
    )
}
