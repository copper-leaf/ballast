package com.copperleaf.ballast.debugger.idea.ui.debugger.injector

import com.copperleaf.ballast.debugger.idea.BallastIntellijPluginInjector
import com.copperleaf.ballast.debugger.idea.ui.debugger.router.DebuggerRouter
import com.copperleaf.ballast.debugger.idea.ui.debugger.vm.DebuggerUiViewModel
import com.copperleaf.ballast.debugger.server.vm.DebuggerServerViewModel
import com.intellij.openapi.project.Project
import kotlinx.coroutines.CoroutineScope

interface DebuggerToolWindowInjector {

    val project: Project
    val debuggerRouter: DebuggerRouter
    val debuggerServerViewModel: DebuggerServerViewModel
    val debuggerUiViewModel: DebuggerUiViewModel

    companion object {
        fun getInstance(
            project: Project,
            toolWindowCoroutineScope: CoroutineScope
        ): DebuggerToolWindowInjector {
            return DebuggerToolWindowInjectorImpl(
                pluginInjector = BallastIntellijPluginInjector.getInstance(project),
                toolWindowCoroutineScope = toolWindowCoroutineScope,
            )
        }
    }
}
