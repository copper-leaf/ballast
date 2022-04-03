package com.copperleaf.ballast.debugger.ui.debugger

import com.copperleaf.ballast.debugger.idea.settings.IdeaPluginPrefs
import com.copperleaf.ballast.savedstate.PerformSaveStateScope
import com.copperleaf.ballast.savedstate.SavedStateAdapter
import org.jetbrains.compose.splitpane.SplitPaneState

class DebuggerSavedStateAdapter(
    private val prefs: IdeaPluginPrefs,
) : SavedStateAdapter<
    DebuggerContract.Inputs,
    DebuggerContract.Events,
    DebuggerContract.State> {

    override suspend fun PerformSaveStateScope<
        DebuggerContract.Inputs,
        DebuggerContract.Events,
        DebuggerContract.State>.save() {
        diff({ connectionsPanePercentageValue }) {
            prefs.connectionsPanePercentage = it
        }
        diff({ viewModelsPanePercentageValue }) {
            prefs.viewModelsPanePercentage = it
        }
        diff({ eventsPanePercentageValue }) {
            prefs.eventsPanePercentage = it
        }
        diff({ selectedViewModelContentTab }) {
            prefs.selectedViewModelContentTab = it
        }
    }

    override suspend fun restore(): DebuggerContract.State {
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
