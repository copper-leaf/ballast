package com.copperleaf.ballast.debugger.idea.ui.debugger

import com.copperleaf.ballast.core.BasicViewModel

typealias DebuggerUiViewModel =  BasicViewModel<
        DebuggerUiContract.Inputs,
        DebuggerUiContract.Events,
        DebuggerUiContract.State>
