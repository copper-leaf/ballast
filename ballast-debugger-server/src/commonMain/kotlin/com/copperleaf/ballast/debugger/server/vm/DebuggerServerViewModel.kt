package com.copperleaf.ballast.debugger.server.vm

import com.copperleaf.ballast.core.BasicViewModel

public typealias DebuggerServerViewModel = BasicViewModel<
        DebuggerServerContract.Inputs,
        DebuggerServerContract.Events,
        DebuggerServerContract.State>
