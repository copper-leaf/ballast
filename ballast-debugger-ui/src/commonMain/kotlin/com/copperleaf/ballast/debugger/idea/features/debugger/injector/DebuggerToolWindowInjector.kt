package com.copperleaf.ballast.debugger.idea.features.debugger.injector

import com.copperleaf.ballast.debugger.idea.features.debugger.router.DebuggerRouter
import com.copperleaf.ballast.debugger.idea.features.debugger.vm.DebuggerUiViewModel
import com.copperleaf.ballast.debugger.server.vm.DebuggerServerViewModel

public interface DebuggerToolWindowInjector {

    public val debuggerRouter: DebuggerRouter
    public val debuggerServerViewModel: DebuggerServerViewModel
    public val debuggerUiViewModel: DebuggerUiViewModel

    public companion object
}
