package com.copperleaf.ballast.debugger.ui.debugger

import com.copperleaf.ballast.debugger.idea.settings.BallastPluginPrefs
import com.copperleaf.ballast.savedstate.RestoreStateScope
import com.copperleaf.ballast.savedstate.SaveStateScope
import com.copperleaf.ballast.savedstate.SavedStateAdapter

class DebuggerSavedStateAdapter(
    private val prefs: BallastPluginPrefs,
) : SavedStateAdapter<
    DebuggerContract.Inputs,
    DebuggerContract.Events,
    DebuggerContract.State> {

    override suspend fun SaveStateScope<
        DebuggerContract.Inputs,
        DebuggerContract.Events,
        DebuggerContract.State>.save() {
    }

    override suspend fun RestoreStateScope<
        DebuggerContract.Inputs,
        DebuggerContract.Events,
        DebuggerContract.State>.restore(): DebuggerContract.State {
        return DebuggerContract.State(
            port = prefs.debuggerPort,
        )
    }

    override suspend fun onRestoreComplete(restoredState: DebuggerContract.State): DebuggerContract.Inputs {
        return DebuggerContract.Inputs.StartServer(restoredState.port)
    }
}
