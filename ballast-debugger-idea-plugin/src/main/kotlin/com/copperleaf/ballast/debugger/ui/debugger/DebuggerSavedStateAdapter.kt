package com.copperleaf.ballast.debugger.ui.debugger

import com.copperleaf.ballast.debugger.idea.settings.IdeaPluginPrefs
import com.copperleaf.ballast.savedstate.RestoreStateScope
import com.copperleaf.ballast.savedstate.SaveStateScope
import com.copperleaf.ballast.savedstate.SavedStateAdapter
import org.jetbrains.compose.splitpane.SplitPaneState

class DebuggerSavedStateAdapter(
    private val prefs: IdeaPluginPrefs,
) : SavedStateAdapter<
    DebuggerContract.Inputs,
    DebuggerContract.Events,
    DebuggerContract.State> {

    override suspend fun SaveStateScope<
        DebuggerContract.Inputs,
        DebuggerContract.Events,
        DebuggerContract.State>.save() {
        saveDiff({ connectionsPanePercentageValue }) {
            prefs.connectionsPanePercentage = it
        }
        saveDiff({ viewModelsPanePercentageValue }) {
            prefs.viewModelsPanePercentage = it
        }
        saveDiff({ eventsPanePercentageValue }) {
            prefs.eventsPanePercentage = it
        }
        saveDiff({ selectedViewModelContentTab }) {
            prefs.selectedViewModelContentTab = it
        }
    }

    override suspend fun RestoreStateScope<
        DebuggerContract.Inputs,
        DebuggerContract.Events,
        DebuggerContract.State>.restore(): DebuggerContract.State {
        return DebuggerContract.State(
            connectionsPanePercentage = SplitPaneState(prefs.connectionsPanePercentage, true),
            viewModelsPanePercentage = SplitPaneState(prefs.viewModelsPanePercentage, true),
            eventsPanePercentage = SplitPaneState(prefs.eventsPanePercentage, true),
            selectedViewModelContentTab = prefs.selectedViewModelContentTab,
        )
    }

    override suspend fun onRestoreComplete(restoredState: DebuggerContract.State): DebuggerContract.Inputs {
        return DebuggerContract.Inputs.StartServer(9684)
    }
}
