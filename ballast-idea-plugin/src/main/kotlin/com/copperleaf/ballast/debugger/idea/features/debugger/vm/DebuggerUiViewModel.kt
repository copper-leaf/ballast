package com.copperleaf.ballast.debugger.idea.features.debugger.vm

import com.copperleaf.ballast.core.BasicViewModel

typealias DebuggerUiViewModel =  BasicViewModel<
        DebuggerUiContract.Inputs,
        DebuggerUiContract.Events,
        DebuggerUiContract.State>
